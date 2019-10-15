package com.bgsoftware.superiorprison.api.data.mine;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.util.SPLocation;

import java.util.Set;

public interface SuperiorMine {

    MineEnum getType();

    String getName();

    SPLocation getMinPoint();

    SPLocation getHighPoint();

    SPLocation getSpawnPoint();

    MineGenerator getGenerator();

    int getPlayerCount();

    Set<Prisoner> getPrisoners();

}
