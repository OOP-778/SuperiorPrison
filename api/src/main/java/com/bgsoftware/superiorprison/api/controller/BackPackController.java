package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import org.bukkit.inventory.ItemStack;

public interface BackPackController {

    // Check if the itemStack is an backpack
    boolean isBackPack(ItemStack itemStack);

    /*
    Returns a backpack from the itemStack
    Can throw an error if the itemStack is not an backpack
    */
    BackPack getBackPack(ItemStack itemStack) throws IllegalArgumentException;
}
