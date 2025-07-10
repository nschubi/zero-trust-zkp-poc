package de.schulzebilk.zkp.core.dto;

import java.math.BigInteger;

public record RegisterProverDTO(
        String proverId,
        BigInteger proverKey) {
}
