package com.bgsoftware.superiorprison.plugin.menu.backpack;

import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.main.util.data.pair.OPair;

public interface BackpackLockable {

    SPrisoner getViewer();

    default void updateBackpackAndUnlock() {
        getViewer().getOpenedBackpack().get().getValue().update();
    }

}
