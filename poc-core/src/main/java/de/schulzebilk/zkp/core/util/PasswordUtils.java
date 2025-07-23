package de.schulzebilk.zkp.core.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    private static final String HASH_ALGORITHM = "SHA-256";

    public static byte[] calculateHash(String message) {
        MessageDigest sha256 = null;
        try {
            sha256 = MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return sha256.digest(message.getBytes(StandardCharsets.UTF_8));
    }

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
        byte[] hash = calculateHash(password);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.mod(mod);
    }

    public static String calcualtePasswordHash(String password) {
        byte[] hash = calculateHash(password);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
