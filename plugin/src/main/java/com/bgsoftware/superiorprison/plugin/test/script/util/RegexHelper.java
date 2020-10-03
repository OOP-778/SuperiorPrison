package com.bgsoftware.superiorprison.plugin.test.script.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHelper {
    private static final Pattern replaceAllButNotNumber = Pattern.compile("[^\\d.]");

    public static boolean matches(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find())
            return true;
        return false;
    }

    public static Integer removeNonNumberAndParse(String in) {
        return Values.parseAsInt(replaceAllButNotNumber.matcher(in).replaceAll(""));
    }
}
