package com.bgsoftware.superiorprison.api.data.mine.shop;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public interface ShopItem {

    // Get price of shop item
    BigDecimal getPrice();

    // Get ItemStack of shop item
    ItemStack getItem();

}
