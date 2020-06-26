package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface SuperiorNms {
    void setBlock(Chunk chunk, Location location, OMaterial material);

    void refreshChunks(World world, Map<Chunk, Set<Location>> locations, Collection<Player> players);
}
