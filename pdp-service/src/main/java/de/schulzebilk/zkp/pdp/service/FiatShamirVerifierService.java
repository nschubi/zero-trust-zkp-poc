package de.schulzebilk.zkp.pdp.service;

import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.model.Signature;
import de.schulzebilk.zkp.core.util.MathUtils;
import de.schulzebilk.zkp.core.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import de.schulzebilk.zkp.pdp.model.FiatShamirRound;
import de.schulzebilk.zkp.pdp.model.Session;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FiatShamirVerifierService {

    private final static Logger LOG = LoggerFactory.getLogger(FiatShamirVerifierService.class);

    private final Map<String, Session> activeSessions;
    private final Map<String, Session> sessionsByProver;
    private final Map<String, BigInteger> proverKeys;
    private final BigInteger publicMod;

    public FiatShamirVerifierService() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.sessionsByProver = new ConcurrentHashMap<>();
        this.proverKeys = new ConcurrentHashMap<>();
        BigInteger q;
        BigInteger p;
        BigInteger mod = BigInteger.ZERO;
        while (mod.equals(BigInteger.ZERO)) {
            q = new BigInteger(MathUtils.BIT_LENGTH, 100, new SecureRandom());
            p = new BigInteger(MathUtils.BIT_LENGTH, 100, new SecureRandom());
            mod = q.multiply(p);
        }
        this.publicMod = mod;
    }

    public BigInteger getPublicMod() {
        return publicMod;
    }

    public void registerProver(String proverId, BigInteger proverKey) {
        if (proverKeys.containsKey(proverId)) {
            throw new IllegalArgumentException("Prover with ID " + proverId + " is already registered.");
        }
        LOG.info("Registering prover with ID: {}", proverId);
        proverKeys.put(proverId, proverKey);
    }

    public Session createSession(String proverId, String target, int threshold) {
        if (!proverKeys.containsKey(proverId)) {
            throw new IllegalArgumentException("Prover with ID " + proverId + " is not registered.");
        }
        Session session = new Session(proverId, target, threshold, proverKeys.get(proverId), publicMod);

        activeSessions.put(session.getSessionId(), session);
        sessionsByProver.put(proverId, session);

        return session;
    }


    public boolean generateChallenge(String sessionId, BigInteger commitment) {
        Session session = activeSessions.get(sessionId);
        if (session == null || session.getCurrentRound() == null) {
            throw new IllegalStateException("No active session found or no current round available for this session. Session ID: " + sessionId);
        }

        FiatShamirRound currentRound = session.getCurrentRound();
        currentRound.setCommitment(commitment);

        boolean challenge = new SecureRandom().nextBoolean();
        currentRound.setChallenge(challenge);

        session.setState(SessionState.WAITING_FOR_RESPONSE);

        return challenge;
    }

    public boolean verifyResponse(String sessionId, BigInteger response) {
        Session session = activeSessions.get(sessionId);
        if (session == null) {
            return false;
        }

        FiatShamirRound currentRound = session.getCurrentRound();
        currentRound.setResponse(response);

        boolean isValid = performVerification(session, currentRound);
        currentRound.setVerified(isValid);

        if (isValid) {
            session.setState(SessionState.COMPLETED);
            session.evaluateVerification();
        } else {
            session.setState(SessionState.FAILED);
        }

        return isValid;
    }

    private boolean performVerification(Session session, FiatShamirRound round) {
        BigInteger res = round.getResponse().pow(2).mod(session.getPublicMod());
        if (round.isChallenge()) {
            BigInteger reserg = round.getCommitment().multiply(session.getProverKey()).mod(session.getPublicMod());
            return res.equals(reserg);
        } else {
            return res.equals(round.getCommitment());
        }
    }

    public boolean checkSignature(Session session) {
        Signature signature = session.getSignature();
        byte[] messageHash = generateHash(signature.message(), signature.commitments());
        boolean[] challenges = generateChallenges(messageHash, signature.responses().length);

        boolean isValid = false;
        for (int i = 0; i < signature.responses().length; i++) {
            BigInteger res = signature.responses()[i].pow(2).mod(session.getPublicMod());
            if (challenges[i]) {
                BigInteger reserg = signature.commitments()[i].multiply(session.getProverKey()).mod(session.getPublicMod());
                if (!res.equals(reserg)) {
                    session.setState(SessionState.FAILED);
                    return isValid;
                }
            } else {
                if (!res.equals(signature.commitments()[i])) {
                    session.setState(SessionState.FAILED);
                    return isValid;
                }
            }
        }
        session.setState(SessionState.VERIFIED);
        return true;
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

    public Session getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public Session getSessionByProver(String proverId) {
        return sessionsByProver.get(proverId);
    }

}
