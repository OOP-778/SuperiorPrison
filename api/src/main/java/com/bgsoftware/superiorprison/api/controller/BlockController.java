package com.bgsoftware.superiorprison.api.controller;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;

public interface BlockController {
    /**
     * Handles the block break of provided blocks synchronously
     * Calls MineBlockBreakEvent or MineMultiBlockBreakEvent
     * @param who who is breaking the block
     * @param locations the locations involved in the block breaking
     */
    void syncHandleBlockBreak(Player who, Location ...locations);

    /**
     * Handles the block break of provided blocks asynchronously
     * Calls MineBlockBreakEvent or MineMultiBlockBreakEvent
     * @param who who is breaking the block
     * @param data
     */
    void asyncHandleBlockBreak(Player who, Map<Location, Map<Material, Integer>> data);
}
