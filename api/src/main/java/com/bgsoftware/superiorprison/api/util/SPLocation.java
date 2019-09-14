package com.bgsoftware.superiorprison.api.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class SPLocation implements Serializable {

    private int x;
    private int y;
    private int z;
    private String worldName;

    public Location toBukkit() {
        World world = getWorld();
        if(world == null) return null;

        return new Location(world, x, y, z);
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public SPLocation(Location location) {
        this.x = location.getBlockX();
        this.z = location.getBlockZ();
        this.y = location.getBlockY();
        worldName = location.getWorld().getName();
    }

}
