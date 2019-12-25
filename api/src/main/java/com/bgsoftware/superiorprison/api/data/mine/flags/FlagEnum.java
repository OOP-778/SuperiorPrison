package com.bgsoftware.superiorprison.api.data.mine.flags;

public enum FlagEnum {

    PVP(false),
    NIGHT_VISION(true),
    TELEPORT_ON_RESET(true),
    PLACE_BLOCKS(false),
    FALL_DAMAGE(false);

    private boolean defaultValue;

    FlagEnum(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

}
