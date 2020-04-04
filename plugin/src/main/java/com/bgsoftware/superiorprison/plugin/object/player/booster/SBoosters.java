package com.bgsoftware.superiorprison.plugin.object.player.booster;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import com.bgsoftware.superiorprison.api.data.player.booster.Boosters;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.google.common.collect.Sets;
import com.oop.orangeengine.main.gson.GsonUpdateable;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SBoosters implements Boosters, Attachable<SPrisoner> {

    @Getter
    private SPrisoner prisoner;

    private Set<Booster> boosters = Sets.newConcurrentHashSet();

    @Override
    public boolean hasActiveBoosters() {
        return !boosters.isEmpty();
    }

    @Override
    public void removeBooster(Booster booster) {
        boosters.remove(booster);
        prisoner.save(true);
    }

    @Override
    public SBooster addBooster(Class<? extends Booster> boosterClazz, long validTill, double rate) {
        SBooster booster;
        if (boosterClazz.isAssignableFrom(SDropsBooster.class))
            booster = new SDropsBooster(validTill, rate);
        else
            booster = new SMoneyBooster(validTill, rate);

        addBooster(booster);
        return booster;
    }

    @Override
    public void addBooster(Booster booster) {
        boosters.add(booster);
        prisoner.save(true);
    }

    @Override
    public <T extends Booster> Set<T> findBoostersBy(Class<T> type) {
        return boosters
                .stream()
                .filter(booster -> type.isAssignableFrom(booster.getClass()))
                .map(booster -> (T) booster)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Booster> getBoosters() {
        return new HashSet<>(boosters);
    }

    @Override
    public void attach(SPrisoner obj) {
        this.prisoner = obj;
    }
}
