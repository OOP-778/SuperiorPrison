package com.bgsoftware.superiorprison.api.data.mine.area;

import com.bgsoftware.superiorprison.api.data.mine.flags.Flag;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import org.bukkit.Location;
import org.bukkit.World;

public interface Area {

    SPLocation getMinPoint();

    World getWorld();

    SPLocation getHighPoint();

    boolean isInside(Location location);

    boolean getFlagState(Flag flag);

    void setFlagState(Flag flag, boolean enabled);

    AreaEnum getType();

}
