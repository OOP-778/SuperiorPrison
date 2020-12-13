package com.bgsoftware.superiorprison.api.data.mine.flags;

import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.google.common.collect.Sets;

import java.util.Set;

public enum Flag {

    PVP(false, "Should players be able to hit each other"),
    FALL_DAMAGE(false, "Toggle fall damage"),
    HUNGER(true, "Toggle hunger");

    private final boolean defaultValue;
    private final Set<AreaEnum> areas;
    private String description;

    Flag(boolean defaultValue, String description) {
        this.defaultValue = defaultValue;
        this.description = description;
        this.areas = Sets.newHashSet(AreaEnum.MINE, AreaEnum.REGION);
    }

    Flag(boolean defaultValue, String description, AreaEnum... areas) {
        this.defaultValue = defaultValue;
        this.areas = Sets.newHashSet(areas);
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public Set<AreaEnum> getAreas() {
        return areas;
    }

    public String getDescription() {
        return description;
    }
}
