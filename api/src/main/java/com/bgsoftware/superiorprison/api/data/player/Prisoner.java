package com.bgsoftware.superiorprison.api.data.player;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.player.booster.Boosters;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.util.Pair;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;
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

    List<Rank> getRanks();

    List<Rank> getSpecialRanks();

    List<LadderRank> getLadderRanks();

    LadderRank getCurrentLadderRank();

    void remove();

    void save(boolean async);

    BigDecimal getPrice(ItemStack itemStack);

    Set<SuperiorMine> getMines();

    void addRank(Rank ...rank);

    void addRank(String ...rank);

    void removeRank(Rank ...rank);

    void removeRank(String ...rank);

    boolean hasRank(String name);

    Set<Prestige> getPrestiges();

    void addPrestige(Prestige ...prestige);

    void addPrestige(String ...prestige);

    void removePrestige(Prestige ...prestige);

    void removePrestige(String ...prestige);

    boolean hasPrestige(String prestige);

    Optional<Prestige> getCurrentPrestige();
}
