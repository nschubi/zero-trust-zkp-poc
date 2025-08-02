package de.schulzebilk.zkp.pdp.model;

import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.model.Signature;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class FiatShamirSession extends Session {
    private final String target;
    private final List<FiatShamirRound> rounds;

    private final int threshold;
    private final BigInteger proverKey;
    private final BigInteger publicMod;

    private Signature signature;

    public FiatShamirSession(String proverId, String target, int threshold, BigInteger proverKey, BigInteger publicMod) {
        super(proverId);
        this.target = target;
        this.rounds = new ArrayList<>();
        this.threshold = threshold;
        this.proverKey = proverKey;
        this.publicMod = publicMod;
    }

    public FiatShamirRound getCurrentRound() {
        if (rounds.isEmpty()) {
            return null;
        }
        return rounds.getLast();
    }

    public void startNewRound() {
        rounds.add(new FiatShamirRound());
        state = SessionState.WAITING_FOR_COMMITMENT;
    }

    public void waitForSignature() {
        state = SessionState.WAITING_FOR_SIGNATURE;
    }

    public void evaluateVerification() {
        var completedRounds = rounds.stream().filter(FiatShamirRound::isVerified).toList().size();
        if (completedRounds >= threshold) {
            state = SessionState.VERIFIED;
        }
    }

    public boolean isSessionActive() {
        return state != SessionState.COMPLETED && state != SessionState.VERIFIED && state != SessionState.FAILED;
    }

    // Getters und Setters
    public String getSessionId() {
        return sessionId;
    }

    public String getProverId() {
        return userId;
    }

    public String getTarget() {
        return target;
    }

    public List<FiatShamirRound> getRounds() {
        return rounds;
    }

    public BigInteger getProverKey() {
        return proverKey;
    }

    public BigInteger getPublicMod() {
        return publicMod;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public Signature getSignature() {
        return signature;
    }
}
