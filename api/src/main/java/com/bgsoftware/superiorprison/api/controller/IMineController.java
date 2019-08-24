package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.mine.ISuperiorMine;
import com.oop.orangeengine.main.util.OptionalConsumer;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface IMineController {

    Set<ISuperiorMine> getMines();

    default Set<ISuperiorMine> getMinesFiltered(Predicate<ISuperiorMine> minesFilter) {
        return getMines().stream()
                .filter(minesFilter)
                .collect(Collectors.toSet());
    }

    default OptionalConsumer<ISuperiorMine> getMineByName(String mineName) {
        return getMineByFilter(mine -> mine.getName().equals(mineName));
    }

    default OptionalConsumer<ISuperiorMine> getMineByFilter(Predicate<ISuperiorMine> mineFilter) {
        return OptionalConsumer.of(getMines().stream()
                .filter(mineFilter)
                .findFirst());
    }
}
