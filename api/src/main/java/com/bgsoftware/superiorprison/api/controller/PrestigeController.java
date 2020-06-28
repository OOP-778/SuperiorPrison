package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.player.Prestige;

import java.util.List;
import java.util.Optional;

public interface PrestigeController {

    // Get list of all available prestiges
    List<Prestige> getPrestiges();

    // Get a prestige by name (ignore-case)
    Optional<Prestige> getPrestige(String name);

    // Get a prestige by order
    Optional<Prestige> getPrestige(int order);
}
