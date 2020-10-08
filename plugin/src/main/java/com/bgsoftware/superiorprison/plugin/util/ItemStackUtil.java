package com.bgsoftware.superiorprison.plugin.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackUtil {

    public static boolean isNamed(ItemStack itemStack) {
        if (itemStack == null) return false;

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return false;

        return meta.hasDisplayName() || meta.hasLore() || meta.hasEnchants();
    }

}
