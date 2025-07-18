package de.schulzebilk.zkp.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PasswordService {

    private final static Logger LOG = LoggerFactory.getLogger(PasswordService.class);

    private final Map<String, String> registeredUsers = new HashMap<>();

    public void registerUser(String username, String password) {
        if (registeredUsers.containsKey(username)) {
            throw new IllegalArgumentException("User with username " + username + " is already registered.");
        }
        LOG.info("Registering user with username: {}", username);
        registeredUsers.put(username, password);
    }

    public boolean authenticateUser(String username, String password) {
        if (!registeredUsers.containsKey(username)) {
            LOG.warn("Authentication failed for user: {}", username);
            return false;
        }
        boolean isAuthenticated = registeredUsers.get(username).equals(password);
        if (isAuthenticated) {
            LOG.info("User {} authenticated successfully.", username);
        } else {
            LOG.warn("Authentication failed for user: {}", username);
        }
        return isAuthenticated;
    }

}
