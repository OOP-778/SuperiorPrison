package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.booster.MoneyBooster;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.PrisonerDefaults;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.hook.impl.ShopGuiPlusHook;
import com.bgsoftware.superiorprison.plugin.object.player.booster.SBoosters;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SRank;
import com.bgsoftware.superiorprison.plugin.util.ClassDebugger;
import com.bgsoftware.superiorprison.plugin.util.SPair;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.body.SqlDataBody;
import com.oop.datamodule.util.DataUtil;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.data.cache.OCache;
import com.oop.orangeengine.main.util.data.set.OConcurrentSet;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Getter
    private SPrestige currentPrestige;

    @Getter
    private SLadderRank currentLadderRank;

    @Setter
    @Getter
    private boolean fortuneBlocks = false;

    private OfflinePlayer cachedOfflinePlayer;

    private Player cachedPlayer;

    @Setter
    private Pair<SuperiorMine, AreaEnum> currentMine;

    @Setter
    @Getter
    private SPair<BigDecimal, Long> soldData = new SPair<>(new BigDecimal(0), 0L);

    @Getter
    private String textureValue;

    public SPrisoner() {
    }

    public SPrisoner(UUID uuid) {
        this.uuid = uuid;
        this.currentLadderRank = (SLadderRank) SuperiorPrisonPlugin.getInstance().getRankController().getDefault();
        boosters.attach(this);

        PrisonerDefaults prisonerDefaults = SuperiorPrisonPlugin.getInstance().getMainConfig().getPrisonerDefaults();
        autoSell = prisonerDefaults.isAutoSell();
        autoBurn = prisonerDefaults.isAutoBurn();
        autoPickup = prisonerDefaults.isAutoPickup();
        fortuneBlocks = prisonerDefaults.isFortuneBlocks();
        this.textureValue = getOnlineSkullTexture();
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
    public List<Rank> getSpecialRanks() {
        return ranks
                .stream()
                .map(name -> SuperiorPrisonPlugin.getInstance().getRankController().getRank(name).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public LadderRank getCurrentLadderRank() {
        return currentLadderRank;
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

    public boolean hasRank(Rank rank) {
        if (!(rank instanceof LadderRank))
            return hasRank(rank.getName());
        else
            return currentLadderRank.getOrder() >= ((LadderRank) rank).getOrder();
    }

    @Override
    public boolean hasRank(String name) {
        return ranks.contains(name) || currentLadderRank.getName().contentEquals(name);
    }

    @Override
    public boolean hasPrestige(Prestige prestige) {
        return currentPrestige != null && currentPrestige.getOrder() >= prestige.getOrder();
    }

    @Override
    public void setPrestige(Prestige prestige, boolean applyOnAdd) {
        this.currentPrestige = (SPrestige) prestige;
        if (applyOnAdd)
            ((SPrestige) prestige).onAdd(this);
    }

    @Override
    public void setLadderRank(LadderRank rank, boolean applyOnAdd) {
        this.currentLadderRank = (SLadderRank) rank;
        if (applyOnAdd)
            ((SLadderRank) rank).onAdd(this);
    }

    private OCache<ItemStack, BigDecimal> pricesCache = OCache
            .builder()
            .concurrencyLevel(1)
            .resetExpireAfterAccess(true)
            .expireAfter(5, TimeUnit.SECONDS)
            .build();

    @Override
    public BigDecimal getPrice(ItemStack itemStack) {
        BigDecimal bigDecimal = pricesCache.get(itemStack);
        if (bigDecimal != null) return bigDecimal;

        final BigDecimal[] price = new BigDecimal[]{new BigDecimal(0)};
        if (SuperiorPrisonPlugin.getInstance().getMainConfig().isUseMineShopsByRank()) {
            price[0] = getMines().stream()
                    .filter(mine -> mine.hasRank(getCurrentLadderRank().getName()) && (!getCurrentPrestige().isPresent() || mine.hasPrestige(getCurrentPrestige().get().getName())))
                    .map(mine -> mine.getShop().getPrice(itemStack))
                    .findFirst()
                    .orElse(new BigDecimal(0));

        } else
            for (SuperiorMine mine : getMines()) {
                BigDecimal minePrice = mine.getShop().getPrice(itemStack);
                if (minePrice.compareTo(price[0]) > 0)
                    price[0] = minePrice;
            }

        if (price[0].doubleValue() == 0 && SuperiorPrisonPlugin.getInstance().getMainConfig().isShopGuiAsFallBack())
            SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> ShopGuiPlusHook.class, hook -> price[0] = new BigDecimal(hook.getPriceFor(itemStack, getPlayer())));

        getBoosters().findBoostersBy(MoneyBooster.class).forEach(booster -> price[0] = price[0] = price[0].multiply(BigDecimal.valueOf(booster.getRate())));
        bigDecimal = price[0];
        pricesCache.put(itemStack, bigDecimal);
        return bigDecimal;
    }

    public void addRank(Rank... rank) {
        for (Rank rank1 : rank) {
            if (rank1 instanceof LadderRank) {
                if (currentLadderRank.getOrder() < ((LadderRank) rank1).getOrder()) {
                    currentLadderRank = (SLadderRank) rank1;
                    ((SRank) rank1).onAdd(this);
                }
                continue;
            }

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
                        if (rank2 instanceof LadderRank) {
                            if (currentLadderRank.getOrder() < ((LadderRank) rank2).getOrder())
                                currentLadderRank = (SLadderRank) rank2;
                        } else
                            ranks.add(rank2.getName());
                        rank2.onAdd(this);
                    });
        }
    }

    public Optional<Prestige> getCurrentPrestige() {
        return Optional.ofNullable(currentPrestige);
    }

    public void clearRanks() {
        ranks.clear();
        currentLadderRank = (SLadderRank) SuperiorPrisonPlugin.getInstance().getRankController().getDefault();
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
                "currentPrestige",
                "logoutmine",
                "currentLadderRank",
                "texture"
        };
    }

    @Override
    public void remove() {
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SPrisonerHolder.class).remove(this);
        getCurrentMine().ifPresent(pair -> pair.getKey().getPrisoners().remove(this));
    }

    @Override
    public void serialize(SerializedData data) {
        data.write("uuid", uuid);
        data.write("autoburn", autoBurn);
        data.write("autopickup", autoPickup);
        data.write("autosell", autoSell);
        data.write("fortuneblocks", fortuneBlocks);
        data.write("ranks", ranks);
        data.write("currentLadderRank", currentLadderRank.getName());
        data.write("currentPrestige", currentPrestige != null ? currentPrestige.getOrder() : null);
        data.write("boosters", boosters);
        data.write("logoutmine", logoutMine);
        data.write("texture", textureValue);
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

        this.logoutMine = data.getElement("logoutmine").map(jsonElement -> jsonElement.isJsonNull() ? null : jsonElement).map(JsonElement::getAsString).orElse(null);
        this.boosters = data.applyAs("boosters", SBoosters.class);
        this.boosters.attach(this);

        // Update data
        if (data.has("currentLadderRank")) {
            this.currentLadderRank = (SLadderRank) SuperiorPrisonPlugin.getInstance().getRankController()
                    .getLadderRank(data.getElement("currentLadderRank").get().getAsString())
                    .orElseThrow(() -> new IllegalStateException("Failed to find rank by " + data.getElement("currentLadderRank").get().getAsString()));
        } else {
            List<LadderRank> foundLadderRanks = new ArrayList<>();
            for (String stringRank : ranks) {
                Optional<Rank> optionalRank = SuperiorPrisonPlugin.getInstance().getRankController().getRank(stringRank);
                if (optionalRank.isPresent()) {
                    Rank rank = optionalRank.get();
                    if (rank instanceof LadderRank) {
                        ranks.remove(stringRank);
                        foundLadderRanks.add((LadderRank) rank);
                    }
                }
            }

            this.currentLadderRank = (SLadderRank) foundLadderRanks.stream()
                    .max(Comparator.comparingInt(LadderRank::getOrder))
                    .orElse(null);
        }

        data.getElement("currentPrestige").ifPresent(currentPrestige -> {
            if (currentPrestige.isJsonPrimitive())
                this.currentPrestige = (SPrestige) SuperiorPrisonPlugin.getInstance().getPrestigeController()
                        .getPrestige(DataUtil.fromElement(currentPrestige, int.class))
                        .orElseThrow(() -> new IllegalStateException("Failed to find prestige by " + currentPrestige.getAsInt()));

            else if (currentPrestige.isJsonArray()) {
                this.currentPrestige = (SPrestige) data.applyAsCollection("currentPrestige")
                        .map(JsonElement::getAsString)
                        .map(p -> SuperiorPrisonPlugin.getInstance().getPrestigeController().getPrestige(p).orElse(null))
                        .filter(Objects::nonNull)
                        .max(Comparator.comparingInt(Prestige::getOrder))
                        .orElse(null);
            }
        });

        this.textureValue = data.getElement("texture").map(JsonElement::getAsString).orElse(getOnlineSkullTexture());
    }

    @SneakyThrows
    private String getOfflineSkullTexture() {
        URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);

        URLConnection con = url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(new InputStreamReader(con.getInputStream()), JsonObject.class);
        con.getInputStream().close();

        return Optional.ofNullable(jsonObject.get("properties"))
                .map(JsonElement::getAsJsonArray)
                .map(array -> array.get(0))
                .map(JsonElement::getAsJsonObject)
                .map(o -> o.get("value"))
                .map(JsonElement::getAsString)
                .orElse(null);
    }

    @SneakyThrows
    private String getOnlineSkullTexture() {
        if (!isOnline()) return getOfflineSkullTexture();

        Object entityPlayer = OSimpleReflection.getMethod(getPlayer().getClass(), "getHandle").invoke(getPlayer());
        GameProfile profile = (GameProfile) Objects.requireNonNull(OSimpleReflection.getMethod(entityPlayer.getClass(), "getProfile"), "GameProfile field is null").invoke(entityPlayer);
        String encodedMojangTexture = profile.getProperties().get("textures").stream().findFirst().map(Property::getValue).orElse(null);

        return encodedMojangTexture;
    }

    @Override
    public void save(boolean b, Runnable runnable) {
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SPrisonerHolder.class).save(this, true, runnable);
    }

    @Override
    public void save(boolean async) {
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SPrisonerHolder.class).save(this, async);
    }

    public void clearCache() {
        cachedPlayer = null;
    }
}
