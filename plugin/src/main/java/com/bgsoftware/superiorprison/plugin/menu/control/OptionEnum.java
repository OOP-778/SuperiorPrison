package com.bgsoftware.superiorprison.plugin.menu.control;

public enum OptionEnum {
    ACCESS,
    ICON,
    GENERATOR,
    FLAGS,
    SHOP,
    EFFECTS,
    SETTINGS,
    REWARDS,
    MESSAGES;

    public static OptionEnum from(String string) {
        for (OptionEnum type : values())
            if (type.name().equalsIgnoreCase(string))
                return type;
        return null;
    }
}
