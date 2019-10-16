package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.oop.orangeengine.main.util.OptionalConsumer;
import org.bukkit.Location;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface MineController {

    Set<SuperiorMine> getMines();

    OptionalConsumer<SuperiorMine> getMine(String mineName);

    OptionalConsumer<SuperiorMine> getMineAt(Location location);
}
