package com.bgsoftware.superiorprison.plugin.test.script.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHelper {
    public static boolean matches(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }
}
