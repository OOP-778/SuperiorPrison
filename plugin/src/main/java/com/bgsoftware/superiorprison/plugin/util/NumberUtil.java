package com.bgsoftware.superiorprison.plugin.util;

import java.math.BigDecimal;
import java.math.BigInteger;
public class NumberUtil {
    private static final BigDecimal BIG_DECIMAL_THOUSAND = BigDecimal.valueOf(1000);

    public static <T extends Comparable<T>> boolean isMoreOrEquals(T comparing, T to) {
        int i = comparing.compareTo(to);
        return i >= 0;
    }

    public static <T extends Comparable<T>> boolean isLessOrEquals(T comparing, T to) {
        int i = comparing.compareTo(to);
        return i <= 0;
    }

    public static <T extends Comparable<T>> boolean equals(T comparing, T to) {
        int i = comparing.compareTo(to);
        return i == 0;
    }

    public static <T extends Comparable<T>> boolean isMoreThan(T comparing, T to) {
        int i = comparing.compareTo(to);
        return i > 0;
    }

    public static <T extends Comparable<T>> boolean isLessThan(T comparing, T to) {
        int i = comparing.compareTo(to);
        return i < 0;
    }

    public static BigDecimal max(BigDecimal decimal, BigDecimal decimal2) {
        return decimal.max(decimal2);
    }

}
