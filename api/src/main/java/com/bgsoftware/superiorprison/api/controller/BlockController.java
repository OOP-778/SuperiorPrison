package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.event.mine.MultiBlockBreakEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface BlockController {
    /**
     * Handles the block break of provided blocks synchronously
     * Calls MineBlockBreakEvent or MineMultiBlockBreakEvent
     * @param prisoner who is breaking the block
     * @param locations the locations involved in the block breaking
     * @param mine where it's happening at
     * @param tool the tool that was used to break the blocks
     * @return array of drops that weren't handled
     */
    MultiBlockBreakEvent syncHandleBlockBreak(Prisoner prisoner, SuperiorMine mine, ItemStack tool, Location ...locations);
}
