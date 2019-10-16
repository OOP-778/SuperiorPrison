package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import org.bukkit.Location;

import java.util.Optional;
import java.util.Set;

public interface MineController {

    Set<SuperiorMine> getMines();

    Optional<SuperiorMine> getMine(String mineName);

    Optional<SuperiorMine> getMineAt(Location location);
}
