package com.bgsoftware.superiorprison.api.data.mine.flags;

import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.google.common.collect.Sets;

import java.util.Set;

public enum Flag {

    PVP(false),
    NIGHT_VISION(true),
    FALL_DAMAGE(false),
    BUILD(false),
    FLIGHT(false),
    HUNGER(true),
    BREAK(false);

    private boolean defaultValue;
    private Set<AreaEnum> areas;

    Flag(boolean defaultValue) {
        this.defaultValue = defaultValue;
        this.areas = Sets.newHashSet(AreaEnum.MINE, AreaEnum.REGION);
    }

    Flag(boolean defaultValue, AreaEnum... areas) {
        this.defaultValue = defaultValue;
        this.areas = Sets.newHashSet(areas);
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public Set<AreaEnum> getAreas() {
        return areas;
    }
}
