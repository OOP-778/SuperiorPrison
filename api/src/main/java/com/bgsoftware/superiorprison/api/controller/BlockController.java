package com.bgsoftware.superiorprison.api.controller;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface BlockController {
    // Let SuperiorPrison handle the blocks that were broken
    void handleBlockBreak(Player who, Location ...locations);
}
