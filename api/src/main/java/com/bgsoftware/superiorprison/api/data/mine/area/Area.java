package com.bgsoftware.superiorprison.api.data.mine.area;

import com.bgsoftware.superiorprison.api.data.mine.flags.Flag;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;

public interface Area {

    Location getMinPoint();

    World getWorld();

    Location getHighPoint();

    boolean isInside(Location location);

    boolean getFlagState(Flag flag);

    void setFlagState(Flag flag, boolean enabled);

    AreaEnum getType();

    Map<Flag, Boolean> getFlags();

}
