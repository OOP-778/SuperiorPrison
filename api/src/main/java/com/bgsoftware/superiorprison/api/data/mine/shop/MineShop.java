package com.bgsoftware.superiorprison.api.data.mine.shop;

import org.bukkit.inventory.ItemStack;

import java.util.Set;

public interface MineShop {

    <T extends ShopItem> Set<T> getItems();

    void addItem(ItemStack itemStack, double price);

    void removeItem(ShopItem item);

}
