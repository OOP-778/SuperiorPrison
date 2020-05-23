package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.booster.MoneyBooster;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.hook.impl.ShopGuiPlusHook;
import com.bgsoftware.superiorprison.plugin.object.player.booster.SBoosters;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SRank;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SSpecialRank;
import com.google.gson.JsonElement;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.body.SqlDataBody;
import com.oop.orangeengine.main.util.data.set.OConcurrentSet;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.bgsoftware.superiorprison.plugin.util.AccessUtil.findPrestige;
import static com.bgsoftware.superiorprison.plugin.util.AccessUtil.findRank;

@Accessors(chain = true)
public class SPrisoner implements com.bgsoftware.superiorprison.api.data.player.Prisoner, SqlDataBody {

    @Setter
    private @NonNull UUID uuid;

    @Getter
    @Setter
    private boolean autoBurn = false;

    @Getter
    @Setter
    private boolean autoSell = false;

    @Setter
    private SBoosters boosters = new SBoosters();

    @Setter
    @Getter
    private String logoutMine;

    @Getter
    @Setter
    private boolean autoPickup = false;

    private final Set<String> ranks = new OConcurrentSet<>();
    private final Set<String> prestiges = new OConcurrentSet<>();

    @Setter
    @Getter
    private boolean fortuneBlocks = false;

    private OfflinePlayer cachedOfflinePlayer;

    private Player cachedPlayer;

    @Setter
    private Pair<SuperiorMine, AreaEnum> currentMine;

    private BigDecimal soldMoney = new BigDecimal(0);

    public SPrisoner() {
    }

    public SPrisoner(UUID uuid) {
        this.uuid = uuid;
        ranks.add(SuperiorPrisonPlugin.getInstance().getRankController().getDefault().getName());
        boosters.attach(this);
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
    public void removeRank(Rank... rank) {
        for (Rank rank1 : rank) {
            ranks.remove(rank1.getName());
            ((SRank) rank1).onRemove(this);

            if (rank1 instanceof LadderRank)
                ((SLadderRank) rank1).getAllNext().forEach(rank2 -> {
                    ranks.remove(rank2.getName());
                    ((SRank) rank2).onRemove(this);
                });
        }
        save(true);
    }

    @Override
    public void removeRank(String... rank) {
        for (String name : rank) {
            findRank(name)
                    .map(rank2 -> (SRank) rank2)
                    .ifPresent(rank2 -> {
                        ranks.remove(rank2.getName());
                        rank2.onRemove(this);
                    });
        }
        save(true);
    }

    @Override
    public boolean hasRank(String name) {
        return ranks.contains(name);
    }

    @Override
    public BigDecimal getPrice(ItemStack itemStack) {
        final BigDecimal[] price = new BigDecimal[]{new BigDecimal(0)};
        for (SuperiorMine mine : getMines()) {
            BigDecimal minePrice = mine.getShop().getPrice(itemStack);
            if (minePrice.compareTo(price[0]) > 0)
                price[0] = minePrice;
        }

        if (price[0].doubleValue() == 0 && SuperiorPrisonPlugin.getInstance().getMainConfig().isShopGuiAsFallBack())
            SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> ShopGuiPlusHook.class, hook -> price[0] = new BigDecimal(hook.getPriceFor(itemStack, getPlayer())));

        getBoosters().findBoostersBy(MoneyBooster.class).forEach(booster -> price[0] = price[0] = price[0].multiply(BigDecimal.valueOf(booster.getRate())));
        return price[0];
    }

    public void addRank(Rank... rank) {
        for (Rank rank1 : rank) {
            ranks.add(rank1.getName());
            ((SRank) rank1).onAdd(this);
        }
    }

    @Override
    public void addRank(String... rank) {
        for (String name : rank) {
            findRank(name)
                    .map(rank2 -> (SRank) rank2)
                    .ifPresent(rank2 -> {
                        ranks.add(rank2.getName());
                        rank2.onAdd(this);
                    });
        }
    }

    public Set<Prestige> getPrestiges() {
        return prestiges
                .stream()
                .map(name -> SuperiorPrisonPlugin.getInstance().getPrestigeController().getPrestige(name).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void addPrestige(Prestige... prestige) {
        for (Prestige prestige1 : prestige) {
            prestiges.add(prestige1.getName());
            ((SPrestige) prestige1).onAdd(this);
        }
    }

    @Override
    public void addPrestige(String... prestige) {
        for (String name : prestige) {
            findPrestige(name).map(prestige2 -> (SPrestige) prestige2).ifPresent(prestige2 -> {
                prestiges.add(prestige2.getName());
                prestige2.onAdd(this);
            });
        }
    }

    public void removePrestige(Prestige... prestige) {
        for (Prestige prestige1 : prestige) {
            prestiges.remove(prestige1.getName());
            ((SPrestige) prestige1).onRemove(this);
        }
    }

    @Override
    public void removePrestige(String... prestige) {
        for (String name : prestige) {
            findPrestige(name).map(prestige2 -> (SPrestige) prestige2).ifPresent(prestige2 -> {
                prestiges.remove(prestige2.getName());
                prestige2.onRemove(this);
            });
        }
    }

    @Override
    public boolean hasPrestige(String prestige) {
        return prestiges.contains(prestige);
    }

    public Optional<Prestige> getCurrentPrestige() {
        return getPrestiges()
                .stream()
                .max(Comparator.comparingInt(Prestige::getOrder));
    }

    public void clearRanks() {
        ranks.clear();
    }

    public void removeRankIf(Predicate<Rank> filter) {
        getRanks()
                .stream()
                .filter(filter)
                .forEach(rank -> ranks.remove(rank.getName()));
        save(true);
    }

    public void clearPrestiges() {
        prestiges.clear();
    }

    @Override
    public String getTable() {
        return "prisoners";
    }

    @Override
    public String getPrimaryKey() {
        return uuid.toString();
    }

    @Override
    public String[] getStructure() {
        return new String[]{
                "uuid",
                "autoburn",
                "autopickup",
                "autosell",
                "fortuneblocks",
                "ranks",
                "boosters",
                "prestiges",
                "logoutmine"
        };
    }

    @Override
    public void remove() {
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SPrisonerHolder.class).remove(this);
    }

    @Override
    public void serialize(SerializedData data) {
        data.write("uuid", uuid);
        data.write("autoburn", autoBurn);
        data.write("autopickup", autoPickup);
        data.write("autosell", autoSell);
        data.write("fortuneblocks", fortuneBlocks);
        data.write("ranks", ranks);
        data.write("prestiges", prestiges);
        data.write("boosters", boosters);
        data.write("logoutmine", logoutMine);
    }

    @Override
    public void deserialize(SerializedData data) {
        this.uuid = data.applyAs("uuid", UUID.class);
        this.autoBurn = data.applyAs("autoburn", boolean.class);
        this.autoPickup = data.applyAs("autopickup", boolean.class);
        this.autoSell = data.applyAs("autosell", boolean.class);
        this.fortuneBlocks = data.applyAs("fortuneblocks", boolean.class);
        this.ranks.addAll(
                data.applyAsCollection("ranks")
                        .map(JsonElement::getAsString)
                        .collect(Collectors.toSet())
        );
        this.prestiges.addAll(
                data.applyAsCollection("prestiges")
                        .map(JsonElement::getAsString)
                        .collect(Collectors.toSet())
        );
        this.logoutMine = data.getElement("logoutmine").map(jsonElement -> jsonElement.isJsonNull() ? null : jsonElement).map(JsonElement::getAsString).orElse(null);
        this.boosters = data.applyAs("boosters", SBoosters.class);
        this.boosters.attach(this);
    }

    @Override
    public void save(boolean b, Runnable runnable) {
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SPrisonerHolder.class).save(this, true, runnable);
    }

    @Override
    public void save(boolean async) {
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SPrisonerHolder.class).save(this, async);
    }
}
