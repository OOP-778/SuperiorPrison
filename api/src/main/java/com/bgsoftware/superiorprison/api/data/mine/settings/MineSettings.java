package com.bgsoftware.superiorprison.api.data.mine.settings;

import com.bgsoftware.superiorprison.api.data.mine.flags.MineFlag;

import java.util.Map;

public interface MineSettings {

    /*
    Get how much players can be inside a mine
    */
    int getPlayerLimit();

    /*
    Get reset settings
    */
    ResetSettings getResetSettings();

    /*
    Get flags
    */
    Map<MineFlag, Boolean> getFlags();

    default boolean isFlagToggled(MineFlag flag) {
        return getFlags().get(flag);
    }

}
