package de.schulzebilk.zkp.core.dto;

import java.math.BigInteger;
import java.util.List;

public record SignatureDTO(String message, BigInteger[] commitments, BigInteger[] responses) {

}
