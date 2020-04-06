package com.bgsoftware.superiorprison.plugin.util;

import java.time.*;
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
        } catch (Exception ignored) {}

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

        long[] longs = calculateTime(between.getSeconds());
        List<String> times = new ArrayList<>();
        times.add(longs[0] + "s");
        times.add(longs[1] + "m");
        times.add(longs[2] + "h");
        times.add(longs[3] + "d");
        Collections.reverse(times);

        times.removeIf(time -> time.startsWith("0"));
        return String.join(", ", times);
    }

    public static long[] calculateTime(long seconds) {
        long sec = seconds % 60;
        long minutes = seconds % 3600 / 60;
        long hours = seconds % 86400 / 3600;
        long days = seconds / 86400;

        return new long[]{sec, minutes, hours, days};
    }

    public static boolean hasExpired(long instant) {
        ZonedDateTime date = getDate(instant);
        Duration between = Duration.between(getDate(), date);
        return between.getSeconds() <= 0;
    }
}
