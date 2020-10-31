package com.bgsoftware.superiorprison.api.controller;


import com.bgsoftware.superiorprison.api.data.player.LadderObject;

import java.util.List;
import java.util.Optional;

public interface PrestigeController {

    // Get list of all available prestiges
    List<LadderObject> getPrestiges();

    // Get max prestige index
    int getMaxIndex();

    // Get a prestige by name (ignore-case)
    Optional<LadderObject> getPrestige(String name);

    // Get a prestige by index
    Optional<LadderObject> getPrestige(int index);
}
