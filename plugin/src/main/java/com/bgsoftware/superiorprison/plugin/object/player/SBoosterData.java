package com.bgsoftware.superiorprison.plugin.object.player;

import com.oop.orangeengine.main.gson.GsonUpdateable;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SBoosterData implements com.bgsoftware.superiorprison.api.data.player.BoosterData, GsonUpdateable {

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
