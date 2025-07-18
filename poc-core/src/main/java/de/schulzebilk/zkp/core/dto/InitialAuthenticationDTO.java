package de.schulzebilk.zkp.core.dto;

public record InitialAuthenticationDTO(AuthenticationDTO authenticationDTO, String method, String endpoint) {
}
