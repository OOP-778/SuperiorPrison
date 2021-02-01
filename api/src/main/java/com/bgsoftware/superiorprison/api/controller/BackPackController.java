package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import java.util.List;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface BackPackController {

  // Check if the itemStack is an backpack
  boolean isBackPack(@NonNull ItemStack itemStack);

  /*
  Returns a backpack from the itemStack
  Can throw an error if the itemStack is not an backpack
  */
  BackPack getBackPack(@NonNull ItemStack itemStack, @NonNull Player owner)
      throws IllegalArgumentException;

  /*
  Returns backpack at a slot
  */
  BackPack getBackPack(int slot, @NonNull Player player);

  // Check if the backpacks are set to be player bound
  boolean isPlayerBound();

  // Find all the backpacks inside player inventory
  List<BackPack> findBackPacks(Player player);
}
