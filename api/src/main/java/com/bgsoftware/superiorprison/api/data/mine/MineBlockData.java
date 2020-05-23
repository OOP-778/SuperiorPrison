package com.bgsoftware.superiorprison.api.data.mine;

import org.bukkit.Material;

public interface MineBlockData {
    long getBlocksLeft();

    long getMaterialLeft(Material material);

    void decrease(Material material, long amount);

    int getPercentageLeft();
}
