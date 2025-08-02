package de.schulzebilk.zkp.pdp.model;

import de.schulzebilk.zkp.core.auth.SessionState;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Session {
    protected final String sessionId;
    protected final String userId;

    protected SessionState state;
    protected final LocalDateTime createdAt;

    public Session(String userId) {
        this.userId = userId;
        this.sessionId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.state = SessionState.INITIALIZED;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
