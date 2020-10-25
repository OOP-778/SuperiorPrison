package com.bgsoftware.superiorprison.plugin.commands.mines.link;

import java.util.Arrays;

public enum Option {
    SHOP,
    GENERATOR,
    SETTINGS,
    EFFECTS,
    MESSAGES,
    REWARDS,
    ALL;

    public static Option match(String in) {
        return Arrays.stream(values())
                .filter(tt -> tt.name().equalsIgnoreCase(in))
                .findFirst()
                .orElse(null);
    }
}