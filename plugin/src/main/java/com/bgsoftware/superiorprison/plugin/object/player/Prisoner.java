package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.api.data.player.IPrisoner;
import com.oop.orangeengine.database.object.DatabaseObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
public class Prisoner extends DatabaseObject implements IPrisoner {

    private @NonNull UUID uuid;
    private boolean isAutoSell = false;
    private BoosterData boosterData = new BoosterData();

    private OfflinePlayer cachedOfflinePlayer;

    public Prisoner(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean isAutoSell() {
        return isAutoSell;
    }

    @Override
    public BoosterData getBoosterData() {
        return boosterData;
    }

    @Override
    public boolean isOnline() {
        ensurePlayerNotNull();
        return cachedOfflinePlayer.isOnline();
    }

    @Override
    public OfflinePlayer getOfflinePlayer() {
        ensurePlayerNotNull();
        return cachedOfflinePlayer.getPlayer();
    }

    private void ensurePlayerNotNull() {
        if (cachedOfflinePlayer == null)
            cachedOfflinePlayer = Bukkit.getOfflinePlayer(uuid);
    }
}
