package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.oop.orangeengine.main.util.OptionalConsumer;
import org.bukkit.Location;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface MineController {

    Set<SuperiorMine> getMines();

    default Set<SuperiorMine> getMinesFiltered(Predicate<SuperiorMine> minesFilter) {
        return getMines().stream()
                .filter(minesFilter)
                .collect(Collectors.toSet());
    }

    default OptionalConsumer<SuperiorMine> getMineByName(String mineName) {
        return getMineByFilter(mine -> mine.getName().equals(mineName));
    }

    default OptionalConsumer<SuperiorMine> getMineByFilter(Predicate<SuperiorMine> mineFilter) {
        return OptionalConsumer.of(getMines().stream()
                .filter(mineFilter)
                .findFirst());
    }

    default OptionalConsumer<SuperiorMine> getMineAtLocation(Location location) {
        return getMineByFilter(mine -> mine.isInside(location));
    }
}
