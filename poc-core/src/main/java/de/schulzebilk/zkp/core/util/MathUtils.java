package de.schulzebilk.zkp.core.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;

public class MathUtils {

    public static final int BIT_LENGTH = 128;
    public static final double THRESHOLD = 0.999999;

    /**
     * Generates a random BigInteger within the specified range.
     *
     * Source: https://stackoverflow.com/questions/2290057/how-to-generate-a-random-biginteger-value-in-java
     *
     * @param rangeStart The start of the range (inclusive).
     * @param rangeEnd   The end of the range (inclusive).
     * @return A random BigInteger between rangeStart and rangeEnd.
     */
    public static BigInteger getRandomBigInteger(BigInteger rangeStart, BigInteger rangeEnd){
        Random rand = new SecureRandom();
        int scale = rangeEnd.toString().length();
        String generated = "";
        for(int i = 0; i < rangeEnd.toString().length(); i++){
            generated += rand.nextInt(10);
        }
        BigDecimal inputRangeStart = new BigDecimal("0").setScale(scale, RoundingMode.FLOOR);
        BigDecimal inputRangeEnd = new BigDecimal(String.format("%0" + (rangeEnd.toString().length()) +  "d", 0).replace('0', '9')).setScale(scale, RoundingMode.FLOOR);
        BigDecimal outputRangeStart = new BigDecimal(rangeStart).setScale(scale, RoundingMode.FLOOR);
        BigDecimal outputRangeEnd = new BigDecimal(rangeEnd).add(new BigDecimal("1")).setScale(scale, RoundingMode.FLOOR);

        BigDecimal bd1 = new BigDecimal(new BigInteger(generated)).setScale(scale, RoundingMode.FLOOR).subtract(inputRangeStart);
        BigDecimal bd2 = inputRangeEnd.subtract(inputRangeStart);
        BigDecimal bd3 = bd1.divide(bd2, RoundingMode.FLOOR);
        BigDecimal bd4 = outputRangeEnd.subtract(outputRangeStart);
        BigDecimal bd5 = bd3.multiply(bd4);
        BigDecimal bd6 = bd5.add(outputRangeStart);

        BigInteger returnInteger = bd6.setScale(0, RoundingMode.FLOOR).toBigInteger();
        returnInteger = (returnInteger.compareTo(rangeEnd) > 0 ? rangeEnd : returnInteger);
        return returnInteger;
    }

    /**
     * Generates a random BigInteger between 1 and the specified rangeEnd (inclusive).
     *
     * @param rangeEnd The end of the range (inclusive).
     * @return A random BigInteger between 1 and rangeEnd.
     */
    public static BigInteger getRandomBigInteger(BigInteger rangeEnd){
       return getRandomBigInteger(BigInteger.ONE, rangeEnd);
    }
}
