package com.bgsoftware.superiorprison.plugin.commands.mines.copy;

public enum CopyType {
    SETTINGS,
    GENERATOR,
    SHOP,
    ACCESS;

    public static CopyType from(String string) {
        for (CopyType type : values())
            if (type.name().equalsIgnoreCase(string))
                return type;
        return null;
    }
}
