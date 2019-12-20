package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.oop.orangeengine.database.annotations.DatabaseTable;
import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.object.DatabaseObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

@Accessors(chain = true)
@DatabaseTable(tableName = "prisoners")
public class SPrisoner extends DatabaseObject implements com.bgsoftware.superiorprison.api.data.player.Prisoner {

    @DatabaseValue(columnName = "uuid")
    @Setter
    private @NonNull UUID uuid;

    @DatabaseValue(columnName = "isAutoSell")
    @Setter
    private boolean isAutoSell = false;

    @DatabaseValue(columnName = "data")
    @Setter
    private SBoosterData boosterData = new SBoosterData();

    @DatabaseValue(columnName = "logoutInMine")
    @Setter
    private boolean logoutInMine = false;

    private transient OfflinePlayer cachedOfflinePlayer;
    private transient Player cachedPlayer;

    @Setter
    private transient SuperiorMine currentMine;

    protected SPrisoner() {}

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

    @Override
    public Player getPlayer() {
       if (!isOnline())
           throw new IllegalStateException("Failed to get online player cause it's offline! (" + getOfflinePlayer().getName() + ")");

       if (cachedPlayer == null)
           cachedPlayer = Bukkit.getPlayer(uuid);

       return cachedPlayer;
    }

    private void ensurePlayerNotNull() {
        if (cachedOfflinePlayer == null)
            cachedOfflinePlayer = Bukkit.getOfflinePlayer(uuid);
    }

    @Override
    public Optional<SuperiorMine> getCurrentMine() {
        return Optional.ofNullable(currentMine);
    }
}
