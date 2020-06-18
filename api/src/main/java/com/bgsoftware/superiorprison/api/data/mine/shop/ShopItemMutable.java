package com.bgsoftware.superiorprison.api.data.mine.shop;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public interface ShopItemMutable {

    // Get price of the shop item
    BigDecimal getPrice();

    // Set price of the shop item
    void setPrice(BigDecimal price);

    // Get ItemStack
    ItemStack getItemStack();
}
