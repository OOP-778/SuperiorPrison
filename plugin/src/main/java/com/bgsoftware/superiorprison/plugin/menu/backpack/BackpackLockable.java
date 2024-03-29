package com.bgsoftware.superiorprison.plugin.menu.backpack;

import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;

public interface BackpackLockable {

  SPrisoner getViewer();

  default void updateBackpackAndUnlock() {
    if (!getViewer().getOpenedBackpack().isPresent()) return;

    SBackPack sBackPack = getViewer().getOpenedBackpack().get().getValue();
    sBackPack.save();
    sBackPack.update();

    getViewer().unlockBackpack();
  }

  default void updateBackpack() {
    if (!getViewer().getOpenedBackpack().isPresent()) return;

    SBackPack sBackPack = getViewer().getOpenedBackpack().get().getValue();
    sBackPack.save();
    sBackPack.update();
  }
}
