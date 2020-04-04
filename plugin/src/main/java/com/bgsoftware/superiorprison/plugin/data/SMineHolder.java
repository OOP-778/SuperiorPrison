package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.MineHolder;
import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.database.DatabaseController;
import com.oop.orangeengine.database.DatabaseHolder;
import com.oop.orangeengine.database.DatabaseObject;
import org.bukkit.Location;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SMineHolder implements DatabaseHolder<String, SNormalMine>, MineHolder {

    private Map<String, SNormalMine> mineMap = Maps.newConcurrentMap();
    private DatabaseController controller;

    public SMineHolder(DatabaseController controller) {
        this.controller = controller;
    }

    @Override
    public Stream<SNormalMine> dataStream() {
        return mineMap.values().stream();
    }

    @Override
    public String generatePrimaryKey(SNormalMine sNormalMine) {
        return sNormalMine.getName();
    }

    @Override
    public void onAdd(SNormalMine sNormalMine, boolean isNew) {
        mineMap.put(sNormalMine.getName(), sNormalMine);
    }

    @Override
    public void onRemove(SNormalMine sNormalMine) {
        mineMap.remove(sNormalMine.getName());
    }

    @Override
    public Set<Class<? extends DatabaseObject>> getObjectVariants() {
        return Sets.newHashSet(SNormalMine.class);
    }

    @Override
    public DatabaseController getDatabaseController() {
        return controller;
    }

    @Override
    public Set<SuperiorMine> getMines() {
        return dataStream()
                .map(mine -> (SuperiorMine) mine)
                .collect(Collectors.toSet());
    }

    public Set<String> getMinesWorlds() {
        return dataStream()
                .map(mine -> mine.getWorld().getName())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<SuperiorMine> getMine(String mineName) {
        return dataStream()
                .filter(mine -> mine.getName().contentEquals(mineName))
                .map(mine -> (SuperiorMine) mine)
                .findFirst();
    }

    @Override
    public Optional<SuperiorMine> getMineAt(Location location) {
        return dataStream()
                .filter(mine -> mine.isInside(location))
                .map(mine -> (SuperiorMine) mine)
                .findFirst();
    }

    public List<SNormalMine> getMinesFor(SPrisoner prisoner) {
        return dataStream()
                .filter(mine -> prisoner.getPlayer().isOp() || mine.canEnter(prisoner))
                .sorted(Comparator.comparing(SNormalMine::getName))
                .collect(Collectors.toList());
    }
}
