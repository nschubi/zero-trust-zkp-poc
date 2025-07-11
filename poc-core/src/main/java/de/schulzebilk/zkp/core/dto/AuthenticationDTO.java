package de.schulzebilk.zkp.core.dto;

import de.schulzebilk.zkp.core.auth.SessionState;

public record AuthenticationDTO(String proverId, String sessionId, String payload, SessionState sessionState) {
}
