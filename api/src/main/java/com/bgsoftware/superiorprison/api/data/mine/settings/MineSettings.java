package com.bgsoftware.superiorprison.api.data.mine.settings;

public interface MineSettings {

    /*
    Get how much players can be inside a mine
    */
    int getPlayerLimit();

    /*
    Get reset settings
    */
    ResetSettings getResetSettings();

}
