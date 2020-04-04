package com.bgsoftware.superiorprison.api.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;

import java.io.Serializable;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@AllArgsConstructor
public class SPLocation implements Serializable, Cloneable {

    private double x;
    private double y;
    private double z;
    private String worldName;

    public SPLocation(Location location) {
        this.x = location.getBlockX();
        this.z = location.getBlockZ();
        this.y = location.getBlockY();
        worldName = location.getWorld().getName();
    }

    public Location toBukkit() {
        World world = getWorld();
        if (world == null) return null;

        return new Location(world, x, y, z);
    }

    public SPLocation add(double x, double y, double z) {
        this.x = this.x + x;
        this.y = this.y + y;
        this.z = this.z + z;
        return this;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    @Override
    public String toString() {
        return "(world: " + worldName + ", x: " + x + ", y: " + y + ", z: " + z + ")";
    }

    @Override
    public SPLocation clone() {
        try {
            return (SPLocation) super.clone();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public int xBlock() {
        return NumberConversions.floor(x);
    }

    public int yBlock() {
        return NumberConversions.floor(y);
    }

    public int zBlock() {
        return NumberConversions.floor(z);
    }
}
