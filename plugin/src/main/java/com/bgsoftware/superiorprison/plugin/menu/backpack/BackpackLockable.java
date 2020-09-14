package com.bgsoftware.superiorprison.plugin.menu.backpack;

import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.main.util.data.pair.OPair;

public interface BackpackLockable {

    SPrisoner getViewer();

    default void updateBackpackAndUnlock() {
        OPair<Integer, SBackPack> pair =
                getViewer().getOpenedBackpack().get();
        getViewer().getPlayer().getInventory().setItem(pair.getFirst(), pair.getSecond().getItem());
        getViewer().unlockBackpack();
    }

}
