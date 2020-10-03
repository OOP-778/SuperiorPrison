package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;

interface BlockData {
    Location getLocation();

    OMaterial getMaterial();

    int getAmount();

    List<ItemStack> getCustomDrops();
}
