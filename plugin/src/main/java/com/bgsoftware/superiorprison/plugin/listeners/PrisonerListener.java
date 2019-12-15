package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.event.player.PlayerQuitEvent;

public class PrisonerListener {

    public PrisonerListener() {
        SyncEvents.listen(PlayerQuitEvent.class, event -> {
            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().insertOrGetPrisoner(event.getPlayer());
            if (prisoner.getCurrentMine().isPresent()) {
                prisoner.setLogoutInMine(true);
                prisoner.save(true);
            }
        });
    }

}
