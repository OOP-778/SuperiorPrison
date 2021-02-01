package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.material.OMaterial;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

interface BlockData {
  Location getLocation();

  OMaterial getMaterial();

  int getAmount();

  List<ItemStack> getCustomDrops();
}
