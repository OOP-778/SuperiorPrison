package com.bgsoftware.superiorprison.api.data.mine.shop;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public interface ShopItem {

    BigDecimal getPrice();

    ItemStack getItem();

}
