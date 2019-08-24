package com.bgsoftware.superiorprison.api.data.player;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public interface IPrisoner {

    UUID getUUID();

    boolean isAutoSell();

    IBoosterData getBoosterData();

    boolean isOnline();

    OfflinePlayer getOfflinePlayer();

}
