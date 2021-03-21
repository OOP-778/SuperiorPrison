package com.bgsoftware.superiorprison.api.data.backpack;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.bgsoftware.superiorprison.api.data.tool.Tool;
import org.bukkit.inventory.ItemStack;

public interface BackPack extends Tool {
  // Get the capacity of the backpack
  BigInteger getCapacity();

  // Get current level of the backpack
  int getCurrentLevel();

  // Get how many of the capacity is used
  BigInteger getUsed();

  /*
  Get all the stored itemstacks
  The list is not mutable
  */
  Map<ItemStack, BigInteger> getStored();

  // Get configuration id of the backpack
  String getId();

  /*
  Returns left overs what's not been added
  */
  Map<ItemStack, BigInteger> add(ItemStack... itemStacks);

  /*
  Returns left overs what's not been removed
  */
  Map<ItemStack, BigInteger> remove(ItemStack... itemStacks);

  // Upgrade the backpack to the specified level
  void upgrade(int level);

  // Check if backpack is full
  boolean isFull();
}
