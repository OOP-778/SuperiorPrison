package com.bgsoftware.superiorprison.api.data.mine.sign;

import org.bukkit.Location;

public interface Sign {
    Location getLocation();

    SignType getType();

    boolean isLoaded();

    void update();
}
