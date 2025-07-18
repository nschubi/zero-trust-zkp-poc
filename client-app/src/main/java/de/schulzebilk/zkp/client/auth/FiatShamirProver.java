package de.schulzebilk.zkp.client.auth;

import de.schulzebilk.zkp.client.rest.FiatShamirPepClient;
import de.schulzebilk.zkp.core.auth.AuthType;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.UserDTO;
import de.schulzebilk.zkp.core.model.User;
import de.schulzebilk.zkp.core.util.MathUtils;
import de.schulzebilk.zkp.core.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FiatShamirProver {
    private final BigInteger publicMod;
    private final Map<String, BigInteger> generatorsBySessionId;
    private final FiatShamirPepClient pepClient;

    @Autowired
    public FiatShamirProver(FiatShamirPepClient pepClient) {
        this.pepClient = pepClient;
        this.publicMod = pepClient.getPublicModulus();
        this.generatorsBySessionId = new ConcurrentHashMap<>();
    }

    public AuthenticationDTO handleAuthentication(User user, AuthenticationDTO auth) {
        switch (auth.sessionState()) {
            case WAITING_FOR_COMMITMENT -> {
                BigInteger commitment = generateCommitment(auth.sessionId());
                return new AuthenticationDTO(user.getUsername(), auth.sessionId(), commitment.toString(), auth.sessionState());
            }
            case WAITING_FOR_RESPONSE -> {
                if (auth.payload() == null) {
                    throw new IllegalArgumentException("No challenge provided in payload for session: " + auth.sessionId());
                }
                BigInteger response = generateResponse(auth.sessionId(),user.getSecret(), Boolean.parseBoolean(auth.payload()));
                return new AuthenticationDTO(user.getUsername(), auth.sessionId(), response.toString(), auth.sessionState());
            }
            default ->  throw new IllegalStateException("Unexpected session state: " + auth.sessionState());
        }
    }

    public BigInteger generateCommitment(String sessionId) {
        BigInteger generator = MathUtils.getRandomBigInteger(BigInteger.valueOf(1), this.publicMod.subtract(BigInteger.valueOf(1)));
        generatorsBySessionId.put(sessionId, generator);
        return generator.pow(2).mod(this.publicMod);
    }

    public BigInteger generateResponse(String sessionId, String secret, boolean challenge) {
        if (!generatorsBySessionId.containsKey(sessionId)) {
            throw new IllegalArgumentException("No commitment found for session with ID: " + sessionId);
        }
        if (challenge) {
            var secretNumber = PasswordUtils.convertPasswordToBigInteger(secret, this.publicMod);
            return generatorsBySessionId.get(sessionId).multiply(secretNumber).mod(this.publicMod);
        }
        return generatorsBySessionId.get(sessionId);
    }

    public BigInteger calculateProverKey(String password) {
        var secret = PasswordUtils.convertPasswordToBigInteger(password, publicMod);
        return secret.pow(2).mod(publicMod);
    }

    public String registerProver(User user) {
        return pepClient.registerProver(new UserDTO(user.getUsername(), calculateProverKey(user.getSecret()).toString(), AuthType.FIATSHAMIR));
    }

}
