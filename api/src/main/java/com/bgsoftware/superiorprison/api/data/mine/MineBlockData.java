package com.bgsoftware.superiorprison.api.data.mine;

import com.bgsoftware.superiorprison.api.data.mine.locks.Lock;
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

    // Create new block data block instance
    // This will make so when blocks are locked, they won't be able to be messed with
    Lock newBlockDataLock();

    /**
     * Lock a location with lock instance
     *
     * @param location the lock location
     * @param lock     lock instance
     */
    void lock(Location location, Lock lock);

    // Unlock all locked blocks by BlockDataLock instance
    void unlock(Lock lock);

    // Check if block is locked
    boolean isLocked(Location location);

    // Get a blocks lock at a specific location
    Optional<Lock> getLockAt(Location location);

    // Check if location exists in the block data
    // If the block is air, it will return false
    boolean has(Location location);
}
