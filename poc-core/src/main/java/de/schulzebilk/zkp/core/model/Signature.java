package de.schulzebilk.zkp.core.model;

import java.math.BigInteger;

public record Signature(String message, BigInteger[] commitments, BigInteger[] responses) {

}