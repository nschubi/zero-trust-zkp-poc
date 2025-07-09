package de.schulzebilk.zkp.pdp.model;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class FiatShamirRound {
    private BigInteger commitment;
    private boolean challenge;
    private BigInteger response;
    private final LocalDateTime timestamp;
    private boolean verified;

    public FiatShamirRound() {
        this.timestamp = LocalDateTime.now();
        this.verified = false;
    }

    public BigInteger getCommitment() {
        return commitment;
    }

    public void setCommitment(BigInteger commitment) {
        this.commitment = commitment;
    }

    public boolean isChallenge() {
        return challenge;
    }

    public void setChallenge(boolean challenge) {
        this.challenge = challenge;
    }

    public BigInteger getResponse() {
        return response;
    }

    public void setResponse(BigInteger response) {
        this.response = response;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public String toString() {
        return "FiatShamirRound{" +
                "commitment=" + commitment +
                ", challenge=" + challenge +
                ", response=" + response +
                ", timestamp=" + timestamp +
                ", verified=" + verified +
                '}';
    }
}
