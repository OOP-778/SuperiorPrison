package com.bgsoftware.superiorprison.plugin.object.player;

import com.oop.orangeengine.database.annotations.DatabaseValue;
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
public class SPrisoner extends DatabaseObject implements com.bgsoftware.superiorprison.api.data.player.Prisoner {

    @DatabaseValue(columnName = "uuid")
    private @NonNull UUID uuid;

    @DatabaseValue(columnName = "isAutoSell")
    private boolean isAutoSell = false;

    @DatabaseValue(columnName = "data")
    private SBoosterData boosterData = new SBoosterData();

    private transient OfflinePlayer cachedOfflinePlayer;

    public SPrisoner(UUID uuid) {
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
    public SBoosterData getBoosterData() {
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
