package com.bgsoftware.superiorprison.plugin.util;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TimeUtil {
    public static long toSeconds(String string) {
        long seconds = 0;
        try {
            return Long.parseLong(string);
        } catch (Exception ignored) {
        }

        string = string.toLowerCase();

        List<String> characters = Arrays.stream(string.split("\\d")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        List<String> numbers = Arrays.asList(string.split("\\D"));

        for (int i = 0; i < characters.size(); i++) {
            int amount = Integer.parseInt(numbers.get(i));
            switch (characters.get(i)) {
                case "s":
                    seconds += amount;
                    break;

                case "m":
                    seconds = TimeUnit.MINUTES.toSeconds(amount);
                    break;

                case "h":
                    seconds = TimeUnit.HOURS.toSeconds(amount);
                    break;

                case "d":
                    seconds = TimeUnit.DAYS.toSeconds(amount);
                    break;
            }
        }
        return seconds;
    }

    public static ZonedDateTime getDate() {
        return ZonedDateTime.now(ZoneId.systemDefault());
    }

    public static ZonedDateTime getDate(long epochSeconds) {
        if (epochSeconds == -1) return null;
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }

    private static final DecimalFormat format = new DecimalFormat("##.#");

    public static String toString(double seconds) {
        double[] longs = calculateTime(seconds);
        List<String> times = new ArrayList<>();

        times.add(format.format(longs[0]) + "s");
        times.add(format.format(longs[1]) + "m");
        times.add(format.format(longs[2]) + "h");
        times.add(format.format(longs[3]) + "d");
        Collections.reverse(times);

        // Remove times that are 0
        times.removeIf(time -> time.length() == 2 && time.startsWith("0"));
        return String.join(", ", times);
    }

    public static String leftToString(ZonedDateTime date) {
        return leftToString(date, false);
    }

    public static String leftToString(ZonedDateTime date, boolean reverse) {
        if (date == null) return "unlimited";

        Duration between;
        if (reverse)
            between = Duration.between(date, getDate());

        else
            between = Duration.between(getDate(), date);

        return toString(between.getSeconds());
    }

    public static double[] calculateTime(double seconds) {
        double sec = seconds % 60;
        long minutes = (long) (seconds % 3600 / 60);
        long hours = (long) seconds % 86400 / 3600;
        long days = (long) seconds / 86400;

        return new double[]{sec, minutes, hours, days};
    }

    public static boolean hasExpired(long instant) {
        ZonedDateTime date = getDate(instant);
        Duration between = Duration.between(getDate(), date);
        return between.getSeconds() <= 0;
    }
}
