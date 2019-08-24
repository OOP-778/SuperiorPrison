package com.bgsoftware.superiorprison.api.data.mine;

import com.bgsoftware.superiorprison.api.data.player.IPrisoner;
import com.bgsoftware.superiorprison.api.util.SPLocation;

import java.util.Set;

public interface ISuperiorMine {

    MineEnum getType();

    String getName();

    SPLocation getMinPoint();

    SPLocation getHighPoint();

    SPLocation getSpawnPoint();

    IMineGenerator getGenerator();

    int getPlayerCount();

    Set<IPrisoner> getPrisoners();

}
