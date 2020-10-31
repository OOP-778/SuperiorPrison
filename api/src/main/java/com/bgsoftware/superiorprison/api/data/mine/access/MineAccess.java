package com.bgsoftware.superiorprison.api.data.mine.access;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;

public interface MineAccess {

    // Check if prisoner can enter
    boolean canEnter(Prisoner prisoner);

    // Add a new script condition
    void addScript(String name, String script);

}
