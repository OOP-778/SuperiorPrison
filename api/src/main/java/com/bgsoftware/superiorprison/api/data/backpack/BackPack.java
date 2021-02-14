package com.bgsoftware.superiorprison.api.data.backpack;

import java.util.List;
import java.util.Map;

import com.bgsoftware.superiorprison.api.data.tool.Tool;
import org.bukkit.inventory.ItemStack;

public interface BackPack extends Tool {
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

  // Get configuration id of the backpack
  String getId();

  /*
  Adds itemstacks to the backpack
  Returns an map of items that were added or not
  If the value is 0, the item wasn't added
  Value is how much did it add, key is an itemStack with modified amount
  */
  Map<ItemStack, Integer> add(ItemStack... itemStacks);

  /*
  Removes itemstacks from the backpack
  Returns an map of items that weren't fully removed
  Value is how much it removed and Key is the itemStack with modified amount
  */
  Map<ItemStack, Integer> remove(ItemStack... itemStacks);

  // Upgrade the backpack to the specified level
  void upgrade(int level);

  // Check if backpack is full
  boolean isFull();
}
