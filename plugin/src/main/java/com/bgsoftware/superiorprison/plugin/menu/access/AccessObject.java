package com.bgsoftware.superiorprison.plugin.menu.access;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.data.player.rank.SpecialRank;

public class AccessObject {

    private final Object object;

    public AccessObject(Object object) {
        this.object = object;
    }

    public boolean isRank() {
        return object instanceof Rank;
    }

    public boolean isPrestige() {
        return object instanceof Prestige;
    }

    public <T> T getAs() {
        return (T) object;
    }

    public <T> T getAs(Class<T> type) {
        return (T) object;
    }

    public String getName() {
        return object instanceof Rank ? ((Rank) object).getName() : ((Prestige) object).getName();
    }

    public String getType() {
        return object instanceof LadderRank ? "ladder" : object instanceof SpecialRank ? "special" : "prestige";
    }

    public boolean isInstanceOf(Class clazz) {
        return clazz.isAssignableFrom(object.getClass());
    }

    public String getPrefix() {
        return object instanceof Rank ? ((Rank) object).getPrefix() : ((Prestige) object).getPrefix();
    }
}
