package com.bgsoftware.superiorprison.plugin.util;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.orangeengine.main.Engine;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.TreeMap;

public class NumberUtil {
    private static final BigDecimal BIG_DECIMAL_THOUSAND = BigDecimal.valueOf(1000);

    private static String getSuffixAt(int index) {
        try {
            return SuperiorPrisonPlugin.getInstance()
                    .getMainConfig()
                    .getNumberFormatterSuffixes()
                    .get(index);
        } catch (Throwable throwable) {
            Engine.getEngine().getLogger().printWarning("Invalid suffix at index {}", index);
        }

        return "-N";
    }

    public static String formatBigInt(BigInteger number) {
        return formatBigDecimal(new BigDecimal(number.longValue()));
    }

    private static int findFirstLetterPosition(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (Character.isAlphabetic(input.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    public static BigDecimal formattedToBigDecimal(String input) {
        input = input.replace("_", "");
        int firstLetterPosition = findFirstLetterPosition(input);
        if (firstLetterPosition == -1)
            return new BigDecimal(input);

        String suffix = input.substring(firstLetterPosition);
        int pow = SuperiorPrisonPlugin.getInstance()
                .getMainConfig()
                .getNumberFormatterSuffixes()
                .indexOf(suffix);
        if (pow == -1)
            throw new IllegalStateException("invalid suffix by '" + suffix + "'");

        return new BigDecimal(input.substring(0, firstLetterPosition)).multiply(BIG_DECIMAL_THOUSAND.pow(pow)).stripTrailingZeros();
    }

    public static String formatBigDecimal(BigDecimal number) {
        int suffixIndex = 0;

        String s = number.toPlainString();
        while (s.length() > 3) {
            s = s.substring(3);
            suffixIndex++;
        }

        number = number.divide(BIG_DECIMAL_THOUSAND.pow(suffixIndex));

        // To round it to 2 digits.
        BigDecimal bigDecimal = number;
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_FLOOR);

        // Add the number with the denomination to get the final value.
        return bigDecimal.stripTrailingZeros().toPlainString() + getSuffixAt(suffixIndex);
    }

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

    public static int getPercentageBetween(Number current, Number shouldBe) {
        current = convertToBigDecimal(current);
        shouldBe = convertToBigDecimal(shouldBe);

        if (isMoreOrEquals((BigDecimal) current, (BigDecimal)shouldBe))
            return 100;

        BigDecimal divide = ((BigDecimal) current).divide((BigDecimal) shouldBe, 2, RoundingMode.HALF_UP);
        BigDecimal multiply = divide.multiply(BigDecimal.valueOf(100)).stripTrailingZeros();
        return multiply.intValue();
    }

    public static BigDecimal convertToBigDecimal(Number number) {
        if (!(number instanceof BigDecimal)) {
            if (number instanceof BigInteger)
                number = new BigDecimal((BigInteger) number);
            else
                if (number instanceof Double)
                    number = BigDecimal.valueOf((Double) number);

                else
                    number = new BigDecimal(number.toString());
        }

        return (BigDecimal) number;
    }
}
