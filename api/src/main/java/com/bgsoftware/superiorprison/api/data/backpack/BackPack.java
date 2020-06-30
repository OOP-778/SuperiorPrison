package com.bgsoftware.superiorprison.api.data.backpack;

import com.oop.orangeengine.item.ItemStackUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public interface BackPack {
    // Get the capacity of the backpack
    int getCapacity();

    // Get current level of the backpack
    int getCurrentLevel();

    // Get how many of the capacity is used
    int getUsed();

    /*
    Get all the stored itemstacks
    The list is not mutable
    */
    List<ItemStack> getStored();

    // Get the backpack item
    ItemStack getItem();

    // Get the owner of the backpack
    Player getOwner();

    // Save the changes made to the backpack
    void save();

    /*
    Adds itemstacks to the backpack
    Returns an map of items that weren't added
    */
    Map<ItemStack, Integer> add(ItemStack ...itemStacks);

    /*
    Removes itemstacks from the backpack
    Returns an map of items that weren't fully removed
    */
    Map<ItemStack, Integer> remove(ItemStack ...itemStacks);
}
