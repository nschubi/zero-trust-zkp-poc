package de.schulzebilk.zkp.core.dto;

import de.schulzebilk.zkp.core.model.Signature;

public record SignatureAuthDTO (AuthenticationDTO authenticationDTO, Signature signature){
}
