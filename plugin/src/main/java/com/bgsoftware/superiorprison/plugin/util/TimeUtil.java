package com.bgsoftware.superiorprison.plugin.util;

import java.util.Arrays;
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
}
