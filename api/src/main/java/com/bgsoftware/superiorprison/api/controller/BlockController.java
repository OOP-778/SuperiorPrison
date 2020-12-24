package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.locks.Lock;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.event.mine.MultiBlockBreakEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public interface BlockController {
    /**
     * Handles the block break of provided blocks
     * Calls MultiBlockBreakEvent
     *
     * @param prisoner  who is breaking the block
     * @param locations the locations involved in the block breaking
     * @param mine      where it's happening at
     * @param tool      the tool that was used to break the blocks
     * @param lock      the lock of the blocks, if it won't be passed, it will create one itself.
     * @return array of drops that weren't handled
     */
    MultiBlockBreakEvent handleBlockBreak(Prisoner prisoner, SuperiorMine mine, ItemStack tool, Lock lock, Location... locations);

    MultiBlockBreakEvent handleBlockBreak(Prisoner prisoner, SuperiorMine mine, BlockBreakEvent event);

    /**
     * Break a block for prisoner
     *
     * @param prisoner  who is breaking the block
     * @param locations the locations involved in the block breaking
     * @param mine      where it's happening at
     * @param tool      the tool that was used to break the blocks
     * @return the event caused by the block break
     */
    MultiBlockBreakEvent breakBlock(Prisoner prisoner, SuperiorMine mine, ItemStack tool, Location... locations);

    MultiBlockBreakEvent breakBlock(Prisoner prisoner, SuperiorMine mine, ItemStack tool, Consumer<MultiBlockBreakEvent> beforeCall, Location... locations);
}
