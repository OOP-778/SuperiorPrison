package com.bgsoftware.superiorprison.plugin.util.script.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHelper {
    private static final Pattern replaceAllButNotNumber = Pattern.compile("[^\\d.]");

    public static boolean matches(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }

    public static Integer removeNonNumberAndParse(String in) {
        return Values.parseAsInt(replaceAllButNotNumber.matcher(in).replaceAll(""));
    }

    /**
     * Returns -1 if none matched, returns index of the pattern if matched
     * @param input input we're matching
     * @param patterns list of patterns
     */
    public static int matchFirst(String input, List<Pattern> patterns) {
        Pattern[] patterns1 = patterns.toArray(new Pattern[0]);
        for (int i = 0; i < patterns1.length; i++) {
            Matcher matcher = patterns1[i].matcher(input);
            if (matcher.find())
                return i;
        }

        return -1;
    }
}
