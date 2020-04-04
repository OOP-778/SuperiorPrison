package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.ShopGuiPlusHook;
import com.bgsoftware.superiorprison.plugin.object.player.booster.SBoosters;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SSpecialRank;
import com.google.common.collect.Sets;
import com.oop.orangeengine.database.DatabaseObject;
import com.oop.orangeengine.database.annotation.Column;
import com.oop.orangeengine.database.annotation.PrimaryKey;
import com.oop.orangeengine.database.annotation.Table;
import com.oop.orangeengine.main.util.data.map.OConcurrentMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Accessors(chain = true)
@Table(name = "prisoners")
public class SPrisoner extends DatabaseObject implements com.bgsoftware.superiorprison.api.data.player.Prisoner {

    @PrimaryKey(name = "uuid")
    @Setter
    private @NonNull UUID uuid;

    @Getter
    @Setter
    @Column(name = "autoBurn")
    private boolean autoBurn = false;

    @Getter
    @Column(name = "isAutoSell")
    @Setter
    private boolean autoSell = false;

    @Column(name = "booster")
    @Setter
    private SBoosters boosters = new SBoosters();

    @Column(name = "logoutMine")
    @Setter
    @Getter
    private String logoutMine;

    @Column(name = "autoPickup")
    @Getter
    @Setter
    private boolean autoPickup = false;

    @Getter
    @Column(name = "completedMineRewards")
    private Set<Integer> completedMineRewards = Sets.newHashSet();

    @Getter
    @Column(name = "minedBlocks")
    private OConcurrentMap<Material, Long> minedBlocks = new OConcurrentMap<>();

    @Column(name = "ranks")
    private Set<String> ranks = Sets.newConcurrentHashSet();

    @Column(name = "prestiges")
    private Set<String> prestiges = Sets.newConcurrentHashSet();

    @Setter
    @Getter
    @Column(name = "fortuneBlocks")
    private boolean fortuneBlocks = false;

    private transient OfflinePlayer cachedOfflinePlayer;

    private transient Player cachedPlayer;

    @Setter
    private transient Pair<SuperiorMine, AreaEnum> currentMine;

    protected SPrisoner() {
        runWhenLoaded(() -> boosters.attach(this));
    }

    public SPrisoner(UUID uuid) {
        this.uuid = uuid;
        ranks.add(SuperiorPrisonPlugin.getInstance().getRankController().getDefault().getName());
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public SBoosters getBoosters() {
        return boosters;
    }

    @Override
    public boolean isLoggedOutInMine() {
        return logoutMine != null;
    }

    @Override
    public boolean isOnline() {
        ensurePlayerNotNull();
        return cachedOfflinePlayer.isOnline();
    }

    @Override
    public OfflinePlayer getOfflinePlayer() {
        ensurePlayerNotNull();
        return Objects.requireNonNull(cachedOfflinePlayer, "Cached offline player is null?");
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
    public Optional<Pair<SuperiorMine, AreaEnum>> getCurrentMine() {
        return Optional.ofNullable(currentMine);
    }

    @Override
    public List<Rank> getRanks() {
        return ranks
                .stream()
                .map(name -> SuperiorPrisonPlugin.getInstance().getRankController().getRank(name).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Rank> getSpecialRanks() {
        return getRanks()
                .stream()
                .filter(rank -> rank instanceof SSpecialRank)
                .collect(Collectors.toList());
    }

    @Override
    public List<LadderRank> getLadderRanks() {
        return getRanks()
                .stream()
                .filter(rank -> rank instanceof SLadderRank)
                .map(rank -> (SLadderRank) rank)
                .sorted(Comparator.comparingInt(LadderRank::getOrder))
                .collect(Collectors.toList());
    }

    @Override
    public LadderRank getCurrentLadderRank() {
        return getLadderRanks()
                .stream()
                .max(Comparator.comparingInt(LadderRank::getOrder))
                .orElse(null);
    }

    @Override
    public Set<SuperiorMine> getMines() {
        return new HashSet<>(SuperiorPrisonPlugin.getInstance().getMineController().getMinesFor(this));
    }

    @Override
    public void removeRank(Rank ...rank) {
        for (Rank rank1 : rank) {
            ranks.remove(rank1.getName());
            if (rank1 instanceof LadderRank)
                ((SLadderRank) rank1).getAllNext().forEach(rank2 -> ranks.remove(rank2.getName()));
        }

        save(true);
    }

    @Override
    public void removeRank(String ...rank) {
        ranks.removeAll(Arrays.asList(rank));
        save(true);
    }

    @Override
    public boolean hasRank(String name) {
        return ranks.contains(name);
    }

    @Override
    public double getPrice(ItemStack itemStack) {
        final double[] price = {0};
        for (SuperiorMine mine : getMines()) {
            double minePrice = mine.getShop().getPrice(itemStack);
            if (minePrice > price[0])
                price[0] = minePrice;
        }

        if (price[0] == 0 && SuperiorPrisonPlugin.getInstance().getMainConfig().isShopGuiAsFallBack())
            SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> ShopGuiPlusHook.class, hook -> price[0] = hook.getPriceFor(itemStack, getPlayer()));

        return price[0];
    }

    public void addRank(Rank ...rank) {
        for (Rank rank1 : rank)
            ranks.add(rank1.getName());
        save(true);
    }

    public Set<Prestige> getPrestiges() {
        return prestiges
                .stream()
                .map(name -> SuperiorPrisonPlugin.getInstance().getPrestigeController().getPrestige(name).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void addPrestige(Prestige ...prestige) {
        for (Prestige prestige1 : prestige)
            prestiges.add(prestige1.getName());
        save(true);
    }

    public void removePrestige(Prestige ...prestige) {
        for (Prestige prestige1 : prestige)
            prestiges.remove(prestige1.getName());
        save(true);
    }

    public Optional<Prestige> getCurrentPrestige() {
        return getPrestiges()
                .stream()
                .max(Comparator.comparingInt(Prestige::getOrder));
    }

    public void clearRanks() {
        ranks.clear();
        save(true);
    }

    public void removeRankIf(Predicate<Rank> filter) {
        getRanks()
                .stream()
                .filter(filter)
                .forEach(rank -> ranks.remove(rank.getName()));
        save(true);
    }
}
