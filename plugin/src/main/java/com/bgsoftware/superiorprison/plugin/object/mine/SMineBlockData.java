package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.locks.Lock;
import com.bgsoftware.superiorprison.api.data.mine.MineBlockData;
import com.bgsoftware.superiorprison.plugin.object.mine.locks.SBLocksLock;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.oop.orangeengine.main.util.data.cache.OCache;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SMineBlockData implements Attachable<SMineGenerator>, MineBlockData {

    // Material, current value, starting value
    @Getter
    private final HashMap<Location, OMaterial> locToMaterial = new HashMap<>();

    @Getter
    private final Map<OMaterial, OPair<Long, Long>> materials = new ConcurrentHashMap<>();

    @Getter
    private final OCache<SBLocksLock, Boolean> lockedBlocks = OCache
            .builder()
            .concurrencyLevel(1)
            .expireAfter(5, TimeUnit.SECONDS)
            .build();

    private SMineGenerator generator;

    @Getter
    @Setter
    private long blocksLeft = 0;

    public void reset() {
        locToMaterial.clear();
        materials.clear();
    }

    public void set(Location location, OMaterial material) {
        locToMaterial.put(location, material);
        OPair<Long, Long> data = materials
                .computeIfAbsent(material, in -> new OPair<>(0L, 0L));
        data.set(data.getFirst() + 1, data.getSecond() + 1);
    }

    public boolean isEmpty() {
        return blocksLeft == 0;
    }

    public void remove(Location location) {
        OMaterial material = locToMaterial.get(location);
        if (material == null) return;

        materials.merge(material, new OPair<>(1L, 0L), (f, s) -> f.setFirst(Math.max(f.getFirst() - s.getFirst(), 0L)));
        blocksLeft = Math.max(blocksLeft - 1, 0L);
    }

    @Override
    public Optional<Material> getMaterialAt(Location location) {
        return Optional.ofNullable(locToMaterial.get(location)).map(OMaterial::parseMaterial);
    }

    @Override
    public void attach(SMineGenerator obj) {
        this.generator = obj;
    }

    public Optional<OMaterial> getOMaterialAt(Location location) {
        return Optional.ofNullable(locToMaterial.get(location));
    }

    @Override
    public long getMaterialLeft(Material material) {
        return getMaterialLeft(OMaterial.matchMaterial(material));
    }

    public long getMaterialLeft(OMaterial material) {
        return Optional.ofNullable(materials.get(material)).map(OPair::getFirst).orElse(0L);
    }

    @Override
    public int getPercentageLeft() {
        return (int) (blocksLeft * 100.0 / generator.getBlocksInRegion());
    }

    @Override
    public Lock newBlockDataLock() {
        SBLocksLock sLock = new SBLocksLock();
        lockedBlocks.put(sLock, true);
        return sLock;
    }

    @Override
    public void lock(Location location, Lock lock) {
        ((SBLocksLock) lock).getLockedLocations().add(location);
    }

    @Override
    public void unlock(Lock lock) {
        lockedBlocks.remove((SBLocksLock) lock);
    }

    @Override
    public boolean isLocked(Location location) {
        return getLockAt(location).isPresent();
    }

    @Override
    public Optional<Lock> getLockAt(Location location) {
        return lockedBlocks.keySet().stream().filter(lock -> lock.getLockedLocations().contains(location)).map(lock -> (Lock) lock).findFirst();
    }

    @Override
    public boolean has(Location location) {
        return locToMaterial.containsKey(location);
    }
}
