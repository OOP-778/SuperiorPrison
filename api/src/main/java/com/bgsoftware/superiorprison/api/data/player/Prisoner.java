package com.bgsoftware.superiorprison.api.data.player;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.player.booster.Boosters;
import com.bgsoftware.superiorprison.api.util.Pair;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface Prisoner {
    UUID getUUID();

    boolean isAutoSell();

    Boosters getBoosters();

    String getLogoutMine();

    boolean isLoggedOutInMine();

    boolean isOnline();

    OfflinePlayer getOfflinePlayer();

    Player getPlayer();

    Optional<Pair<SuperiorMine, AreaEnum>> getCurrentMine();

    boolean isAutoPickup();

    boolean isAutoBurn();

    boolean isFortuneBlocks();

    void remove();

    void save(boolean async);

    BigDecimal getPrice(ItemStack itemStack);

    Set<SuperiorMine> getMines();

    void setLadderRank(String name, boolean applyOnAdd);

    void setLadderRank(int index, boolean applyOnAdd);

    void setPrestige(int index, boolean applyOnAdd);

    int getLadderRank();

    int getPrestige();

    LadderObject getParsedLadderRank();

    Optional<LadderObject> getParsedPrestige();

    SuperiorMine getHighestMine();
}
