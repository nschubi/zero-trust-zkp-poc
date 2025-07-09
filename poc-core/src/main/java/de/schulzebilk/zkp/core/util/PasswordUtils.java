package de.schulzebilk.zkp.core.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    public static BigInteger convertToBigInteger(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty");
        }
        try {
            return new BigInteger(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Input string is not a valid BigInteger: " + s, e);
        }
    }

    public static BigInteger convertPasswordToBigInteger(String password, BigInteger mod)  {
        MessageDigest sha256 = null;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hash = sha256.digest(password.getBytes(StandardCharsets.UTF_8));
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.mod(mod);
    }
}
