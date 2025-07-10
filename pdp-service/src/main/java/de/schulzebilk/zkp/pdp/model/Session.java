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

    private BigInteger proverKey;
    private BigInteger publicMod;
    private double threshold;

    public Session(String proverId, String target) {
        this.sessionId = UUID.randomUUID().toString();
        this.proverId = proverId;
        this.target = target;
        this.rounds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.state = SessionState.INITIALIZED;
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
        if (1 - (Math.pow((double) 1 /2, completedRounds)) > threshold) {
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

    public void setProverKey(BigInteger proverKey) {
        this.proverKey = proverKey;
    }

    public BigInteger getPublicMod() {
        return publicMod;
    }

    public void setPublicMod(BigInteger publicMod) {
        this.publicMod = publicMod;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getThreshold() {
        return threshold;
    }
}
