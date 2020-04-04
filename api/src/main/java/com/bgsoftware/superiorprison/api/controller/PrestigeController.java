package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.player.Prestige;

import java.util.List;
import java.util.Optional;

public interface PrestigeController {
    boolean isLoaded();

    List<Prestige> getPrestiges();

    Optional<Prestige> getPrestige(String name);
}
