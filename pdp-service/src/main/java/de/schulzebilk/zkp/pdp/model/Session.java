package de.schulzebilk.zkp.pdp.model;

import de.schulzebilk.zkp.core.auth.SessionState;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Session {
    private final String sessionId;
    private final String proverId;
    private final String target;
    private final List<FiatShamirRound> rounds;
    private final LocalDateTime createdAt;
    private SessionState state;

    private final int threshold;
    private final BigInteger proverKey;
    private final BigInteger publicMod;

    public Session(String proverId, String target, int threshold, BigInteger proverKey, BigInteger publicMod) {
        this.sessionId = UUID.randomUUID().toString();
        this.proverId = proverId;
        this.target = target;
        this.rounds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.state = SessionState.INITIALIZED;
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
        return proverId;
    }

    public String getTarget() {
        return target;
    }

    public List<FiatShamirRound> getRounds() {
        return rounds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public BigInteger getProverKey() {
        return proverKey;
    }

    public BigInteger getPublicMod() {
        return publicMod;
    }

    public double getThreshold() {
        return threshold;
    }
}
