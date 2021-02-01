package com.bgsoftware.superiorprison.api.data.mine.shop;

import java.math.BigDecimal;
import java.util.Set;
import org.bukkit.inventory.ItemStack;

public interface MineShop {

  // Get all the shop items
  <T extends ShopItem> Set<T> getItems();

  // Add an shop item
  void addItem(ItemStack itemStack, BigDecimal price);

  // Remove an shop item
  void removeItem(ShopItem item);

  // Get price of an shop item
  BigDecimal getPrice(ItemStack itemStack);

  // Check if shop contains itemStack
  boolean hasItem(ItemStack itemStack);
}
