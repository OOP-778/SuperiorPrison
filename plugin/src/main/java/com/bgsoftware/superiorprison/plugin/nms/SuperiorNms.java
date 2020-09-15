package com.bgsoftware.superiorprison.plugin.nms;

import com.bgsoftware.superiorprison.plugin.util.SPLocation;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public interface SuperiorNms {
    void setBlock(Chunk chunk, Location location, OMaterial material);

    void refreshChunks(World world, Map<Chunk, Set<Location>> locations, Collection<Player> players);

    default void refreshChunks(World world, Set<SPLocation> locations, Collection<Player> players) {
        Map<OPair<Integer, Integer>, Set<Location>> mapped = new HashMap<>();
        for (SPLocation location : locations)
            mapped
                    .computeIfAbsent(new OPair<>(location.xBlock() >> 4, location.zBlock() >> 4), key -> new HashSet<>())
                    .add(location.toBukkit());


        Map<Chunk, Set<Location>> finalLocations = new HashMap<>();
        mapped.forEach((chunk, l) -> finalLocations.put(world.getChunkAt(chunk.getFirst(), chunk.getSecond()), l));
        refreshChunks(world, finalLocations, players);
    }
}
