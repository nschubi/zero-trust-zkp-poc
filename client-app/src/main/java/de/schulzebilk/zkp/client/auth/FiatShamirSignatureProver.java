package de.schulzebilk.zkp.client.auth;

import de.schulzebilk.zkp.core.model.Signature;
import de.schulzebilk.zkp.core.model.User;
import de.schulzebilk.zkp.core.util.MathUtils;
import de.schulzebilk.zkp.core.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;


@Component
public class FiatShamirSignatureProver {

    FiatShamirProver prover;

    @Autowired
    public FiatShamirSignatureProver(FiatShamirProver prover) {
        this.prover = prover;
    }

    public Signature generateSignature(String sessionId, User user, int rounds) {
        BigInteger[] generators = generateGenerators(rounds);
        BigInteger[] commitments = generateCommitments(generators);

        String message = user.getUsername() + sessionId;
        byte[] messageHash = generateHash(message, commitments);
        boolean[] challenges = generateChallenges(messageHash, rounds);

        BigInteger[] responses = generateResponses(generators, user.getSecret(), challenges);

        return new Signature(message, commitments, responses);
    }

    public boolean checkSignature(Signature signature, String secret) {
        byte[] messageHash = generateHash(signature.message(), signature.commitments());
        boolean[] challenges = generateChallenges(messageHash, signature.responses().length);

        for (int i = 0; i < signature.responses().length; i++) {
            BigInteger res = signature.responses()[i].pow(2).mod(prover.getPublicMod());
            if (challenges[i]) {
                BigInteger reserg = signature.commitments()[i].multiply(prover.calculateProverKey(secret)).mod(prover.getPublicMod());
                if (!res.equals(reserg)) {
                    return false;
                }
            } else {
                if (!res.equals(signature.commitments()[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    private BigInteger[] generateGenerators(int rounds) {
        BigInteger[] generators = new BigInteger[rounds];
        for (int i = 0; i < rounds; i++) {
            generators[i] = MathUtils.getRandomBigInteger(BigInteger.valueOf(1),
                    prover.getPublicMod().subtract(BigInteger.valueOf(1)));
        }
        return generators;
    }

    private BigInteger[] generateCommitments(BigInteger[] generators) {
        BigInteger[] commitments = new BigInteger[generators.length];
        for (int i = 0; i < generators.length; i++) {
            commitments[i] = generators[i].pow(2).mod(prover.getPublicMod());
        }
        return commitments;
    }

    private byte[] generateHash(String message, BigInteger[] commitments) {
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        for (BigInteger commitment : commitments) {
            sb.append(commitment.toString());
        }
        return PasswordUtils.calculateHash(sb.toString());
    }

    private boolean[] generateChallenges(byte[] messageHash, int rounds) {
        boolean[] challenges = new boolean[rounds];
        int byteIndex = 0;
        int bitIndex = 0;
        for (int i = 0; i < rounds; i++) {
            if (byteIndex >= messageHash.length) {
                throw new IllegalArgumentException("Not enough bytes in hash for the number of rounds specified." +
                        " Expected " + rounds + " rounds, but hash length is " + messageHash.length);
            }
            byte currentByte = messageHash[byteIndex];
            boolean challenge = ((currentByte >> bitIndex) & 1) == 1;
            challenges[i] = challenge;

            bitIndex++;
            if (bitIndex == 8) {
                bitIndex = 0;
                byteIndex++;
            }
        }
        return challenges;
    }

    public BigInteger[] generateResponses(BigInteger[] generators, String secret, boolean[] challenges) {
        BigInteger[] responses = new BigInteger[generators.length];
        for (int i = 0; i < generators.length; i++) {
            if (challenges[i]) {
                var secretNumber = PasswordUtils.convertPasswordToBigInteger(secret, prover.getPublicMod());
                responses[i] =
                        generators[i].multiply(secretNumber).mod(prover.getPublicMod());
            } else {
                responses[i] = generators[i];
            }
        }
        return responses;
    }

}
