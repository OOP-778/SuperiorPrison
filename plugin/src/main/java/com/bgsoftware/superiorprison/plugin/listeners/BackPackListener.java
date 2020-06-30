package com.bgsoftware.superiorprison.plugin.listeners;

import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.event.player.PlayerInteractEvent;

public class BackPackListener {

    public BackPackListener() {
        SyncEvents.listen(PlayerInteractEvent.class, event -> {

        });
    }
}
