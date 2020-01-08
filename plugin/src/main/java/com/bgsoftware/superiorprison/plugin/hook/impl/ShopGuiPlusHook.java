package com.bgsoftware.superiorprison.plugin.hook.impl;

import com.bgsoftware.superiorprison.plugin.hook.SHook;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopGuiPlusHook extends SHook {
    public ShopGuiPlusHook(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "ShopGuiPlus";
    }
}
