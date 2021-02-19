package com.bgsoftware.superiorprison.plugin.nms;

import com.bgsoftware.superiorprison.plugin.util.SPLocation;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface SuperiorNms {
  void setBlock(Chunk chunk, Location location, OMaterial material);

  default void setBlockAndUpdate(
      Chunk chunk, Location location, OMaterial material, Collection<Player> players) {}

  void refreshChunks(World world, Map<Chunk, Set<Location>> locations, Collection<Player> players);

  default void refreshChunks(
      World world, Collection<Location> locations, Collection<Player> players) {
    Map<OPair<Integer, Integer>, Set<Location>> mapped = new HashMap<>();
    for (Location location : locations)
      mapped
          .computeIfAbsent(
              new OPair<>(location.getBlockX() >> 4, location.getBlockZ() >> 4),
              key -> new HashSet<>())
          .add(location);

    Map<Chunk, Set<Location>> finalLocations = new HashMap<>();
    mapped.forEach(
        (chunk, l) -> finalLocations.put(world.getChunkAt(chunk.getFirst(), chunk.getSecond()), l));
    StaticTask.getInstance().ensureSync(() -> refreshChunks(world, finalLocations, players));
  }

  default OMaterial getBlockType(Chunk chunk, Location location) {
    return null;
  }

  default void refreshChunks(World world, Map<Chunk, Set<SPLocation>> locations, List<Player> players) {
    Map<Chunk, Set<Location>> convertedIntoBukkit = new HashMap<>();
    locations.forEach((chunk, spLocations) -> {
      convertedIntoBukkit.put(chunk, spLocations.stream().map(loc -> loc.toBukkit(world)).collect(Collectors.toSet()));
    });

    refreshChunks(world, convertedIntoBukkit, players);
  }
}
