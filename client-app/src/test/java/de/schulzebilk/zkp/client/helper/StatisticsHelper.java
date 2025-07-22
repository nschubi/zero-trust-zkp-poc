package de.schulzebilk.zkp.client.helper;

import java.util.List;

public class StatisticsHelper {

    public static void printStatistics(List<Long> durations) {
        double average = calculateAverage(durations);
        double standardDeviation = calculateStandardDeviation(durations);
        double minimum = calculateMinimum(durations);
        double maximum = calculateMaximum(durations);

        System.out.println("Average Duration: " + average + " ms");
        System.out.println("Standard Deviation: " + standardDeviation + " ms");
        System.out.println("Minimum Duration: " + minimum + " ms");
        System.out.println("Maximum Duration: " + maximum + " ms");
    }

    public static double calculateStandardDeviation(List<Long> durations) {
        if (durations.isEmpty()) {
            return 0.0;
        }

        double mean = calculateAverage(durations);

        double variance = durations.stream()
                .mapToDouble(duration -> Math.pow(duration - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }

    public static double calculateAverage(List<Long> durations) {
        if (durations.isEmpty()) {
            return 0.0;
        }
        return durations.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    public static double calculateMinimum(List<Long> durations) {
        if (durations.isEmpty()) {
            return 0.0;
        }
        return durations.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0L);
    }

    public static double calculateMaximum(List<Long> durations) {
        if (durations.isEmpty()) {
            return 0.0;
        }
        return durations.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
    }
}
