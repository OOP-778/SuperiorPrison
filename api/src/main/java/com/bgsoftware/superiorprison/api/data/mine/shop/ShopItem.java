package com.bgsoftware.superiorprison.api.data.mine.shop;

import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface ShopItem {

    double getPrice();

    ItemStack getItem();

    Optional<String> getCommand();

}
