package com.bgsoftware.superiorprison.api.data.mine.shop;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public interface ShopItemWrapped {

    BigDecimal getPrice();

    void setPrice(BigDecimal price);

    ItemStack getItemStack();
}
