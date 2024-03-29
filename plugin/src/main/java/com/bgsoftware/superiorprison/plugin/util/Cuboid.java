package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Cuboid {

  private final Vector minimumPoint, maximumPoint;
  private final String worldName;

  public Cuboid(Cuboid cuboid) {
    this(
        cuboid.worldName,
        cuboid.minimumPoint.getX(),
        cuboid.minimumPoint.getY(),
        cuboid.minimumPoint.getZ(),
        cuboid.maximumPoint.getX(),
        cuboid.maximumPoint.getY(),
        cuboid.maximumPoint.getZ());
  }

  public Cuboid(Location loc) {
    this(loc, loc);
  }

  public Cuboid(Location loc1, Location loc2) {
    if (loc1 != null && loc2 != null) {
      if (loc1.getWorld() != null && loc2.getWorld() != null) {
        if (!loc1.getWorld().getUID().equals(loc2.getWorld().getUID()))
          throw new IllegalStateException(
              "The 2 locations of the cuboid must be in the same world!");
      } else {
        throw new NullPointerException("One/both of the worlds is/are null!");
      }
      this.worldName = loc1.getWorld().getName();

      double xPos1 = Math.min(loc1.getX(), loc2.getX());
      double yPos1 = Math.min(loc1.getY(), loc2.getY());
      double zPos1 = Math.min(loc1.getZ(), loc2.getZ());
      double xPos2 = Math.max(loc1.getX(), loc2.getX());
      double yPos2 = Math.max(loc1.getY(), loc2.getY());
      double zPos2 = Math.max(loc1.getZ(), loc2.getZ());
      this.minimumPoint = new Vector(xPos1, yPos1, zPos1);
      this.maximumPoint = new Vector(xPos2, yPos2, zPos2);
    } else {
      throw new NullPointerException("One/both of the locations is/are null!");
    }
  }

  public Cuboid(
      String worldName, double x1, double y1, double z1, double x2, double y2, double z2) {
    if (worldName == null || Bukkit.getServer().getWorld(worldName) == null)
      throw new NullPointerException("One/both of the worlds is/are null!");
    this.worldName = worldName;

    double xPos1 = Math.min(x1, x2);
    double xPos2 = Math.max(x1, x2);
    double yPos1 = Math.min(y1, y2);
    double yPos2 = Math.max(y1, y2);
    double zPos1 = Math.min(z1, z2);
    double zPos2 = Math.max(z1, z2);
    this.minimumPoint = new Vector(xPos1, yPos1, zPos1);
    this.maximumPoint = new Vector(xPos2, yPos2, zPos2);
  }

  public boolean containsLocation(Location location, boolean withY) {
    if (location == null || !location.getWorld().getName().equals(this.worldName)) return false;

    double cuboidMinX = minimumPoint.getX();
    double cuboidMinY = minimumPoint.getY();
    double cuboidMinZ = minimumPoint.getZ();

    double cuboidHighX = maximumPoint.getX();
    double cuboidHighY = maximumPoint.getY();
    double cuboidHighZ = maximumPoint.getZ();

    return cuboidMinX <= location.getX()
        && cuboidHighX >= location.getX()
        && (!withY || cuboidMinY <= location.getY() && cuboidHighY >= location.getY())
        && cuboidMinZ <= location.getZ()
        && cuboidHighZ >= location.getZ();
  }

  public boolean containsLocation(Location location) {
    return containsLocation(location, true);
  }

  public boolean containsVector(Vector vector) {
    return vector != null && vector.isInAABB(this.minimumPoint, this.maximumPoint);
  }

  public Location getLowerLocation() {
    return this.minimumPoint.toLocation(this.getWorld());
  }

  public double getLowerX() {
    return this.minimumPoint.getX();
  }

  public double getLowerY() {
    return this.minimumPoint.getY();
  }

  public double getLowerZ() {
    return this.minimumPoint.getZ();
  }

  public Location getUpperLocation() {
    return this.maximumPoint.toLocation(this.getWorld());
  }

  public double getUpperX() {
    return this.maximumPoint.getX();
  }

  public double getUpperY() {
    return this.maximumPoint.getY();
  }

  public double getUpperZ() {
    return this.maximumPoint.getZ();
  }

  public double getVolume() {
    return (this.getUpperX() - this.getLowerX() + 1)
        * (this.getUpperY() - this.getLowerY() + 1)
        * (this.getUpperZ() - this.getLowerZ() + 1);
  }

  public World getWorld() {
    World world = Bukkit.getServer().getWorld(this.worldName);
    if (world == null)
      throw new NullPointerException("World '" + this.worldName + "' is not loaded.");
    return world;
  }

  public CompletableFuture<Map<OPair<Integer, Integer>, Set<SPLocation>>> getFutureArrayWithChunks() {
    CompletableFuture<Map<OPair<Integer, Integer>, Set<SPLocation>>> future =
        new CompletableFuture<>();

    // Here we gather all the blocks within mine in an async task
    new OTask()
        .sync(false)
        .runnable(
            () -> {
              Map<OPair<Integer, Integer>, Set<SPLocation>> map = new HashMap<>();

              World world = this.getWorld();
              if (world != null) {
                for (int x = this.minimumPoint.getBlockX();
                    x <= this.maximumPoint.getBlockX();
                    x++) {
                  int chunkX = x >> 4;
                  for (int y = this.minimumPoint.getBlockY();
                      y <= this.maximumPoint.getBlockY() && y <= world.getMaxHeight();
                      y++) {
                    for (int z = this.minimumPoint.getBlockZ();
                        z <= this.maximumPoint.getBlockZ();
                        z++) {
                      int chunkZ = z >> 4;

                      map.computeIfAbsent(new OPair<>(chunkX, chunkZ), pair -> new HashSet<>())
                          .add(new SPLocation(world.getName(), x, y, z));
                    }
                  }
                }
              }
              future.complete(map);
            })
        .execute();

    return future;
  }

  public CompletableFuture<SPLocation[]> getFutureArray() {
    CompletableFuture<SPLocation[]> future = new CompletableFuture<>();

    // Here we gather all the blocks within mine in an async task
    new OTask()
        .sync(false)
        .runnable(
            () -> {
              LinkedList<SPLocation> blocks = new LinkedList<>();
              World world = this.getWorld();
              if (world != null) {
                for (int x = this.minimumPoint.getBlockX();
                    x <= this.maximumPoint.getBlockX();
                    x++) {
                  for (int y = this.minimumPoint.getBlockY();
                      y <= this.maximumPoint.getBlockY() && y <= world.getMaxHeight();
                      y++) {
                    for (int z = this.minimumPoint.getBlockZ();
                        z <= this.maximumPoint.getBlockZ();
                        z++) {
                      blocks.add(new SPLocation(worldName, x, y, z));
                    }
                  }
                }
              }
              future.complete(blocks.toArray(new SPLocation[0]));
            })
        .execute();

    return future;
  }

  public Set<Location> getSphereAt(Location location, int radius) {
    Set<Location> locations = new HashSet<>();

    int X = location.getBlockX();
    int Y = location.getBlockY();
    int Z = location.getBlockZ();

    int minX = X - radius,
        maxX = X + radius,
        minY = Y - radius,
        maxY = Y + radius,
        minZ = Z - radius,
        maxZ = Z + radius;

    World world = location.getWorld();

    for (int x = minX; x <= maxX; x++) {
      int diffXSqr = (X - x) * (X - x);
      for (int y = minY; y <= maxY; y++) {
        if (y > getUpperY()) break;

        int diffYSqr = (Y - y) * (Y - y);
        for (int z = minZ; z <= maxZ; z++) {
          int diffZSqr = (Z - z) * (Z - z);
          Location spLocation = new Location(world, x, y, z);

          if (diffXSqr + diffYSqr + diffZSqr > radius * radius) continue;
          if (!containsVector(spLocation.toVector())) continue;

          locations.add(spLocation);
        }
      }
    }
    return locations;
  }
}
