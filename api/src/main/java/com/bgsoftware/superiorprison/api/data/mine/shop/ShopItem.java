package com.bgsoftware.superiorprison.api.data.mine.shop;

import java.math.BigDecimal;
import org.bukkit.inventory.ItemStack;

public interface ShopItem {

  // Get price of shop item
  BigDecimal getPrice();

  // Get ItemStack of shop item
  ItemStack getItem();
}
