package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.google.common.collect.Sets;
import com.oop.orangeengine.database.annotations.DatabaseTable;
import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.object.DatabaseObject;
import com.oop.orangeengine.main.util.data.map.OConcurrentMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Accessors(chain = true)
@DatabaseTable(tableName = "prisoners")
public class SPrisoner extends DatabaseObject implements com.bgsoftware.superiorprison.api.data.player.Prisoner {

    @DatabaseValue(columnName = "uuid")
    @Setter
    private @NonNull UUID uuid;

    @Getter
    @DatabaseValue(columnName = "isAutoSell")
    @Setter
    private boolean autoSell = false;

    @DatabaseValue(columnName = "data")
    @Setter
    private SBoosterData boosterData = new SBoosterData();

    @DatabaseValue(columnName = "logoutInMine")
    @Setter
    private boolean logoutInMine = false;

    @DatabaseValue(columnName = "autoPickup")
    @Getter
    @Setter
    private boolean autoPickup = false;

    @Getter
    @DatabaseValue(columnName = "completedMineRewards")
    private Set<Integer> completedMineRewards = Sets.newHashSet();

    @Getter
    @DatabaseValue(columnName = "minedBlocks")
    private OConcurrentMap<Material, Long> minedBlocks = new OConcurrentMap<>();

    @DatabaseValue(columnName = "rank")
    private String rank;

    @Getter
    @DatabaseValue(columnName = "fortuneBlocks")
    private boolean fortuneBlocks = false;

    private transient OfflinePlayer cachedOfflinePlayer;

    private transient Player cachedPlayer;

    @Setter
    private transient SuperiorMine currentMine;

    protected SPrisoner() {
    }

    public SPrisoner(UUID uuid) {
        this.uuid = uuid;
        SRank defaultRank = SuperiorPrisonPlugin.getInstance().getRankController().getDefaultRank();
        if (defaultRank != null)
            rank = defaultRank.getName();
    }

    @Override
    public UUID getUUID() {
        return uuid;
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
    public SRank getRank() {
        if (rank == null) {
            SuperiorPrisonPlugin.getInstance().getOLogger().printWarning("Prisoner: " + getOfflinePlayer().getName() + " doesn't seem to have a rank!");
            return null;
        }

        if (!SuperiorPrisonPlugin.getInstance().getRankController().isLoaded()) {
            SuperiorPrisonPlugin.getInstance().getOLogger().printWarning("Trying to get prisoner rank before ranks are loaded!");
            return null;
        }

        SRank rankObj = SuperiorPrisonPlugin.getInstance().getRankController().findRankById(rank).orElse(null);
        if (rankObj == null)
            SuperiorPrisonPlugin.getInstance().getOLogger().printWarning("Failed to find rank by id: " + rank + " for prisoner: " + getOfflinePlayer().getName());

        return rankObj;
    }

    @Override
    public Optional<SuperiorMine> getCurrentMine() {
        return Optional.ofNullable(currentMine);
    }
}
