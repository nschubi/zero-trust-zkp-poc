package de.schulzebilk.zkp.pdp.service;

import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.util.MathUtils;
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
            q = new BigInteger(MathUtils.BIT_LENGTH, new SecureRandom());
            p = new BigInteger(MathUtils.BIT_LENGTH, new SecureRandom());
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
        proverKeys.put(proverId, proverKey);
    }

    public Session createSession(String proverId, String target, int threshold) {
        Session session = new Session(proverId, target, threshold);
        session.setProverKey(proverKeys.get(proverId));
        session.setPublicMod(publicMod);

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

        session.setState(SessionState.CHALLENGE_SENT);

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

    public Session getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public Session getSessionByProver(String proverId) {
        return sessionsByProver.get(proverId);
    }

    public void cleanupExpiredSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);

        activeSessions.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            return session.getCreatedAt().isBefore(cutoff) && !session.isSessionActive();
        });
    }

}
