package com.bgsoftware.superiorprison.plugin.object.player.booster;

import com.bgsoftware.superiorprison.api.data.player.booster.*;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.orangeengine.main.util.data.set.OConcurrentSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@EqualsAndHashCode
public class SBoosters implements Boosters, Attachable<SPrisoner>, SerializableObject {

    private final Set<Booster> boosters = new OConcurrentSet<>();
    @Getter
    private transient SPrisoner prisoner;

    @Override
    public boolean hasActiveBoosters() {
        return !boosters.isEmpty();
    }

    @Override
    public void removeBooster(Booster booster) {
        boosters.remove(booster);
    }

    @Override
    public SBooster addBooster(Class<? extends Booster> boosterClazz, long validTill, double rate) {
        SBooster booster = null;
        if (DropsBooster.class.isAssignableFrom(boosterClazz))
            booster = new SDropsBooster(generateId(), validTill, rate);
        else if (MoneyBooster.class.isAssignableFrom(boosterClazz))
            booster = new SMoneyBooster(generateId(), validTill, rate);
        else if (XPBooster.class.isAssignableFrom(boosterClazz))
            booster = new SXPBooster(generateId(), validTill, rate);

        addBooster(booster);
        return booster;
    }

    @Override
    public void addBooster(Booster booster) {
        boosters.add(booster);
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
    public Optional<Booster> findBoosterBy(int id) {
        return boosters
                .stream()
                .filter(booster -> booster.getId() == id)
                .findFirst();
    }

    @Override
    public Set<Booster> set() {
        return new HashSet<>(boosters);
    }

    @Override
    public void attach(SPrisoner obj) {
        this.prisoner = obj;

        for (Booster booster : set()) {
            if (booster.getId() == 0) {
                ((SBooster) booster).setId(generateId());
            }
        }
    }

    public int generateId() {
        int[] ids = new int[3];
        for (int i = 0; i < ids.length; i++)
            ids[i] = ThreadLocalRandom.current().nextInt(9);

        return Integer.parseInt("" + ids[0] + ids[1] + ids[2]);
    }

    public void clear() {
        boosters.clear();
    }

    @Override
    public void serialize(SerializedData data) {
        data.write("data", boosters);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        boosters.addAll(
                serializedData.applyAsCollection("data")
                        .map(SBooster::fromElement)
                        .collect(Collectors.toSet())
        );
    }
}
