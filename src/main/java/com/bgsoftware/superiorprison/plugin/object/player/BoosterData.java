package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.api.data.player.IBoosterData;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class BoosterData implements IBoosterData, Serializable {

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
