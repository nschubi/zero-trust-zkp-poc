package de.schulzebilk.zkp.core.util;

import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import org.springframework.http.HttpHeaders;

public class AuthUtils {

    public static AuthenticationDTO createAuthenticationDtoFromHeaders(HttpHeaders headers) {
        return new AuthenticationDTO(
                headers.getFirst("auth-user"),
                headers.getFirst("auth-session"),
                headers.getFirst("auth-payload"),
                SessionState.valueOf(headers.getFirst("auth-state")));
    }

    public static HttpHeaders createHeadersFromAuthenticationDto(AuthenticationDTO authenticationDTO) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("auth-user", authenticationDTO.proverId());
        headers.add("auth-session", authenticationDTO.sessionId());
        headers.add("auth-payload", authenticationDTO.payload());
        headers.add("auth-state", authenticationDTO.sessionState().name());
        return headers;
    }
}
