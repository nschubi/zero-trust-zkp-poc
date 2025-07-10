package de.schulzebilk.zkp.core.auth;

public record AuthenticationData(String proverId, String sessionId, String signature, SessionState sessionState) {
}
