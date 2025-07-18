package de.schulzebilk.zkp.core.dto;

import de.schulzebilk.zkp.core.auth.AuthType;


public record UserDTO(
        String userId,
        String secret,
        AuthType authType) {
}
