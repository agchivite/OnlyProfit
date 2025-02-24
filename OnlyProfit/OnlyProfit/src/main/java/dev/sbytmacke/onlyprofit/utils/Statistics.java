package dev.sbytmacke.onlyprofit.utils;

import java.util.Collections;
import java.util.List;

public class Statistics {
    public static Integer calculateMedian(List<Integer> bets) {
        if (bets.isEmpty()) {
            return 0;
        }
        Collections.sort(bets);
        int size = bets.size();
        if (size % 2 == 0) {
            return (bets.get(size / 2 - 1) + bets.get(size / 2)) / 2;
        } else {
            return bets.get(size / 2);
        }
    }

    public static int calculateAverage(List<Integer> numbers) {
        if (numbers.isEmpty()) {
            return 0;
        }
        double sum = numbers.stream().mapToInt(Integer::intValue).sum();
        double average = sum / numbers.size();
        return (int) Math.round(average);
    }
}
