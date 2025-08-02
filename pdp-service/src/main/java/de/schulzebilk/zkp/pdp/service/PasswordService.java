package de.schulzebilk.zkp.pdp.service;

import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.pdp.model.PasswordSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordService {

    private final static Logger LOG = LoggerFactory.getLogger(PasswordService.class);

    private final Map<String, String> registeredUsers = new ConcurrentHashMap<>();
    private final Map<String, PasswordSession> activeSessions = new ConcurrentHashMap<>();


    public PasswordSession createSession(String userId) {
        LOG.info("Creating session for user: {}", userId);
        PasswordSession session = new PasswordSession(userId);
        session.setState(SessionState.WAITING_FOR_PASSWORD);
        activeSessions.put(session.getSessionId(), session);
        return session;
    }

    public void registerUser(String username, String password) {
        if (registeredUsers.containsKey(username)) {
            throw new IllegalArgumentException("User with username " + username + " is already registered.");
        }
        LOG.info("Registering user with username: {}", username);
        registeredUsers.put(username, password);
    }

    public void authenticateUser(String sessionId, String password) {
        PasswordSession session = activeSessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("No session found for sessionId: " + sessionId);
        }
        boolean isAuthenticated = registeredUsers.get(session.getUserId()).equals(password);
        if (isAuthenticated) {
            session.setState(SessionState.VERIFIED);
            LOG.info("User {} authenticated successfully.", sessionId);
        } else {
            session.setState(SessionState.FAILED);
            LOG.warn("Authentication failed for user: {}", sessionId);
        }
    }

    public PasswordSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

}
