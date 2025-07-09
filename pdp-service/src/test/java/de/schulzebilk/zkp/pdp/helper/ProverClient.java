package de.schulzebilk.zkp.pdp.helper;

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
