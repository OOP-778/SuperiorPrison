package com.bgsoftware.superiorprison.api.data.mine.shop;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Set;

public interface MineShop {

    <T extends ShopItem> Set<T> getItems();

    void addItem(ItemStack itemStack, BigDecimal price);

    void removeItem(ShopItem item);

    BigDecimal getPrice(ItemStack itemStack);

    boolean hasItem(ItemStack itemStack);
}
