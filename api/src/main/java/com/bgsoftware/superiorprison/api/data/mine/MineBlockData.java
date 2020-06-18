package com.bgsoftware.superiorprison.api.data.mine;

import org.bukkit.Material;

public interface MineBlockData {
    // Get how many blocks left
    long getBlocksLeft();

    // Get how many of specific material left
    long getMaterialLeft(Material material);

    // Decrease specific material
    void decrease(Material material, long amount);

    // Get percentage of how many blocks left
    int getPercentageLeft();
}
