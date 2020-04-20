package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.MineHolder;
import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.Maps;
import com.oop.datamodule.DataStorage;
import org.bukkit.Location;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SMineHolder extends DataStorage<SNormalMine> implements MineHolder {

    private Map<String, SNormalMine> mineMap = Maps.newConcurrentMap();

    public SMineHolder(DatabaseController controller) {
        super(controller);
    }

    @Override
    public Stream<SNormalMine> stream() {
        return mineMap.values().stream();
    }

    @Override
    public Class<? extends SNormalMine>[] getVariants() {
        return new Class[]{SNormalMine.class};
    }

    @Override
    public void onAdd(SNormalMine sNormalMine) {
        System.out.println("added");
        mineMap.put(sNormalMine.getName(), sNormalMine);
    }

    @Override
    public void onRemove(SNormalMine sNormalMine) {
        mineMap.remove(sNormalMine.getName());
    }

    @Override
    public Set<SuperiorMine> getMines() {
        return getMines(null);
    }

    public Set<SuperiorMine> getMines(Predicate<SuperiorMine> filter) {
        return stream()
                .map(mine -> (SuperiorMine) mine)
                .filter(mine -> filter == null || filter.test(mine))
                .collect(Collectors.toSet());
    }

    public Set<String> getMinesWorlds() {
        return stream()
                .map(mine -> mine.getWorld().getName())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<SuperiorMine> getMine(String mineName) {
        return stream()
                .filter(mine -> mine.getName().contentEquals(mineName))
                .map(mine -> (SuperiorMine) mine)
                .findFirst();
    }

    @Override
    public Optional<SuperiorMine> getMineAt(Location location) {
        return stream()
                .filter(mine -> mine.isInside(location))
                .map(mine -> (SuperiorMine) mine)
                .findFirst();
    }

    public List<SNormalMine> getMinesFor(SPrisoner prisoner) {
        return stream()
                .filter(mine -> prisoner.getPlayer().isOp() || mine.canEnter(prisoner))
                .sorted(Comparator.comparing(SNormalMine::getName))
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<SNormalMine> iterator() {
        return mineMap.values().iterator();
    }
}
