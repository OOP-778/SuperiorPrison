package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import com.bgsoftware.superiorprison.plugin.config.BackPackConfig;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
public class SBackPack implements BackPack {

    private ItemStack itemStack;
    private BackPackConfig config;
    private BackPackData data;

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public int getCurrentLevel() {
        return 0;
    }

    @Override
    public int getUsed() {
        return 0;
    }

    @Override
    public List<ItemStack> getStored() {
        return null;
    }

    @Override
    public ItemStack getItem() {
        return itemStack;
    }
}
