package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import org.bukkit.Location;

import java.util.Optional;
import java.util.Set;

public interface MineController {

    /*
    Get all mines inside the database
    */
    Set<SuperiorMine> getMines();

    /*
    Get mine by specific name
    */
    Optional<SuperiorMine> getMine(String mineName);

    /*
    Get mine at specific location
    */
    Optional<SuperiorMine> getMineAt(Location location);
}
