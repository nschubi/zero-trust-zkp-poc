package de.schulzebilk.zkp.core.model;

import de.schulzebilk.zkp.core.auth.AuthType;

public class User {
    private String username;
    private String secret;
    private AuthType authType;

    public User() {
    }

    public User(String username, String secret, AuthType authType) {
        this.username = username;
        this.secret = secret;
        this.authType = authType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }
}
