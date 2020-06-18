package com.bgsoftware.superiorprison.plugin.hook.impl;

import com.bgsoftware.superiorprison.plugin.hook.SHook;
import com.oop.orangeengine.material.OMaterial;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopGuiPlusHook extends SHook {
    public ShopGuiPlusHook(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "ShopGuiPlus";
    }

    public double getPriceFor(ItemStack itemStack, Player player) {
        return ShopGuiPlusApi.getItemStackPriceSell(player, itemStack);
    }
}
