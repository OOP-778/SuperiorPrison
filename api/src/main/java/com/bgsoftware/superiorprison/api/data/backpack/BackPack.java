package com.bgsoftware.superiorprison.api.data.backpack;

import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

  // Get configuration id of the backpack
  String getId();

  // Get the backpack item
  ItemStack getItem();

  // Get the owner of the backpack
  Player getOwner();

  // Save the changes made to the backpack
  void save();

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

  /*
  Update the backpack in the player inventory
  Also if the player is currently viewing the backpack it will also update the menu
  */
  void update();

  // Upgrade the backpack to the specified level
  void upgrade(int level);

  // Check if backpack was modified
  boolean isModified();

  // Check if backpack is full
  boolean isFull();
}
