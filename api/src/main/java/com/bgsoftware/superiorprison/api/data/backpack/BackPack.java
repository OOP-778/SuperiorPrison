package com.bgsoftware.superiorprison.api.data.backpack;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface BackPack {
    int getCapacity();

    int getCurrentLevel();

    int getUsed();

    List<ItemStack> getStored();

    ItemStack getItem();
}
