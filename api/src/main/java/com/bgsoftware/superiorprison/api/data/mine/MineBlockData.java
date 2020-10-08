package com.bgsoftware.superiorprison.api.data.mine;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Optional;

public interface MineBlockData {
    // Get how many blocks left
    long getBlocksLeft();

    // Get how many of specific material left
    long getMaterialLeft(Material material);

    // Remove at a specific location
    void remove(Location location);
    
    // Get material at a location
    Optional<Material> getMaterialAt(Location location);

    // Get percentage of how many blocks left
    int getPercentageLeft();
}
