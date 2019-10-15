package com.bgsoftware.superiorprison.api.data.player;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public interface Prisoner {

    UUID getUUID();

    boolean isAutoSell();

    BoosterData getBoosterData();

    boolean isOnline();

    OfflinePlayer getOfflinePlayer();

}
