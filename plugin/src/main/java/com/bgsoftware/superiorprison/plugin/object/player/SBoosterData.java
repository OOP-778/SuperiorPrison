package com.bgsoftware.superiorprison.plugin.object.player;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SBoosterData implements com.bgsoftware.superiorprison.api.data.player.BoosterData, Serializable {

    private Set<Double> boosters = ConcurrentHashMap.newKeySet();

    @Override
    public boolean hasActiveBooster() {
        return !boosters.isEmpty();
    }

    @Override
    public Set<Double> getActiveBoosters() {
        return boosters;
    }
}
