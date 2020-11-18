package com.bgsoftware.superiorprison.plugin.util;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

public class NumberUtil {
    private static TreeMap<BigInteger, String> formatters = new TreeMap<>();

    static {
        String[] suffixes = new String[]{
                "K",
                "M",
                "B",
                "T",
                "Q",
                "QT",
                "S",
                "ST",
                "O",
                "N",
                "D",
                "UD",
                "DD",
                "Z"
        };

        BigInteger thousand = BigInteger.valueOf(1000);
        for (int i = 0; i < suffixes.length; i++)
            formatters.put(thousand.pow(i + 1), suffixes[i]);
    }

    public static String formatBigInt(BigInteger integer) {
        Map.Entry<BigInteger, String> entry = formatters.floorEntry(integer);
        if (entry == null) return integer.toString();
        BigInteger key = entry.getKey();
        BigInteger d = key.divide(BigInteger.valueOf(1000));
        BigInteger m = integer.divide(d);
        BigInteger f = m.divide(BigInteger.valueOf(1000));
        return f.toString() + entry.getValue();
    }

    public static boolean isMoreOrEquals(BigInteger comparing, BigInteger to) {
        int i = comparing.compareTo(to);
        return i >= 0;
    }

    public static boolean isLessOrEquals(BigInteger comparing, BigInteger to) {
        int i = comparing.compareTo(to);
        return i <= 0;
    }

    public static boolean equals(BigInteger comparing, BigInteger to) {
        int i = comparing.compareTo(to);
        return i == 0;
    }

    public static boolean isMoreThan(BigInteger comparing, BigInteger to) {
        int i = comparing.compareTo(to);
        return i > 0;
    }

    public static boolean isLessThan(BigInteger comparing, BigInteger to) {
        int i = comparing.compareTo(to);
        return i < 0;
    }
}
