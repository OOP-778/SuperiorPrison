package com.bgsoftware.superiorprison.api.data.mine.area;

import com.bgsoftware.superiorprison.api.data.mine.flags.Flag;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;

public interface Area {

    // Get min point of the area
    Location getMinPoint();

    // Get the world of the area that it's located at
    World getWorld();

    // Get high point of the area
    Location getHighPoint();

    // Check if an location is inside the area
    boolean isInsideWithoutY(Location location);

    // Check flag state
    boolean getFlagState(Flag flag);

    // Set flag state
    void setFlagState(Flag flag, boolean enabled);

    // Get type of area (Either mine or region)
    AreaEnum getType();

    // Get all the flags
    Map<Flag, Boolean> getFlags();

}
