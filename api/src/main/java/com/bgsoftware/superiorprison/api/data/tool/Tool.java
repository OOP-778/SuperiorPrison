package com.bgsoftware.superiorprison.api.data.tool;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Tool {
    /**
     * Get tool item
     * @return tool item
     */
    ItemStack getItem();

    /**
     * Get holder of the tool
     * @return tool holder
     */
    Player getHolder();

    /**
     * Save changes made to the tool
     */
    void save();

    /**
     * Check if backpack was modified
     * @return boolean if the backpack was modified after last save
     */
    boolean isModified();

    /**
     * Update the tool in players inventory
     */
    void update();
}
