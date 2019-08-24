package com.bgsoftware.superiorprison.api.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class SPLocation implements Serializable {

    private int x;
    private int y;
    private int z;
    private String world;

    public Location toBukkit() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

}
