package com.bgsoftware.superiorprison.api.data.player;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface Prisoner {

    UUID getUUID();

    boolean isAutoSell();

    BoosterData getBoosterData();

    boolean isOnline();

    OfflinePlayer getOfflinePlayer();

    Player getPlayer();

    Optional<SuperiorMine> getCurrentMine();

    boolean isAutoPickup();

    PrisonerRank getRank();

}
