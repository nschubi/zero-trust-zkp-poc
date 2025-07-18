package de.schulzebilk.zkp.pdp.helper;

import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.UserDTO;
import de.schulzebilk.zkp.core.util.MathUtils;
import de.schulzebilk.zkp.core.util.PasswordUtils;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProverClient {
    private final BigInteger publicMod;
    private final BigInteger secret;
    private final BigInteger proverKey;
    private final String proverId;

    private final Map<String, BigInteger> generatorsBySessionId;

    public ProverClient(String proverId, BigInteger publicMod, String password) {
        this.proverId = proverId;
        this.publicMod = publicMod;
        this.secret = PasswordUtils.convertPasswordToBigInteger(password, publicMod);
        this.proverKey = this.secret.pow(2).mod(publicMod);
        this.generatorsBySessionId = new ConcurrentHashMap<>();
    }

    public AuthenticationDTO handleAuthentication(AuthenticationDTO auth) {
        switch (auth.sessionState()) {
            case WAITING_FOR_COMMITMENT -> {
                BigInteger commitment = generateCommitment(auth.sessionId());
                return new AuthenticationDTO(proverId, auth.sessionId(), commitment.toString(), auth.sessionState());
            }
            case WAITING_FOR_RESPONSE -> {
                if (auth.payload() == null) {
                    throw new IllegalArgumentException("No challenge provided in payload for session: " + auth.sessionId());
                }
                BigInteger response = generateResponse(auth.sessionId(), Boolean.parseBoolean(auth.payload()));
                return new AuthenticationDTO(proverId, auth.sessionId(), response.toString(), auth.sessionState());
            }
            default ->  throw new IllegalStateException("Unexpected session state: " + auth.sessionState());
        }
    }

    public BigInteger generateCommitment(String sessionId) {
        BigInteger generator = MathUtils.getRandomBigInteger(BigInteger.valueOf(1), this.publicMod.subtract(BigInteger.valueOf(1)));
        generatorsBySessionId.put(sessionId, generator);
        return generator.pow(2).mod(this.publicMod);
    }

    public BigInteger generateResponse(String sessionId, boolean challenge) {
        if (!generatorsBySessionId.containsKey(sessionId)) {
            throw new IllegalArgumentException("No commitment found for session with ID: " + sessionId);
        }
        if (challenge) {
            return generatorsBySessionId.get(sessionId).multiply(this.secret).mod(this.publicMod);
        }
        return generatorsBySessionId.get(sessionId);
    }

    public String getProverId() {
        return proverId;
    }

    public BigInteger getProverKey() {
        return proverKey;
    }

}
