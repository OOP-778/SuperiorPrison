package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.player.LadderObject;
import com.bgsoftware.superiorprison.api.data.player.booster.MoneyBooster;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.main.PrisonerDefaults;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.hook.impl.ShopGuiPlusHook;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.booster.SBoosters;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer;
import com.bgsoftware.superiorprison.plugin.test.Testing;
import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.bgsoftware.superiorprison.plugin.util.Removeable;
import com.bgsoftware.superiorprison.plugin.util.SPair;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.body.MultiTypeBody;
import com.oop.datamodule.gson.JsonElement;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.data.cache.OCache;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SPrisoner implements com.bgsoftware.superiorprison.api.data.player.Prisoner, MultiTypeBody, Removeable {
    private static final String defaultHeadTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmM4ZWExZjUxZjI1M2ZmNTE0MmNhMTFhZTQ1MTkz\n" +
            "YTRhZDhjM2FiNWU5YzZlZWM4YmE3YTRmY2I3YmFjNDAifX19";
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

    @Setter
    private SNormalMine highestMine;

    private String textureValue;

    @Getter
    @Setter
    private boolean removed;

    private OPair<Integer, SBackPack> openedBackpack;
    private final OCache<ItemStack, BigDecimal> pricesCache = OCache
            .builder()
            .concurrencyLevel(1)
            .expireAfter(5, TimeUnit.SECONDS)
            .build();

    @Getter
    private int ladderRank;

    @Getter
    private int prestige;

    public SPrisoner() {
    }

    public SPrisoner(UUID uuid) {
        this.uuid = uuid;
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
    public Set<SuperiorMine> getMines() {
        return new HashSet<>(SuperiorPrisonPlugin.getInstance().getMineController().getMinesFor(this));
    }

    @Override
    public void setLadderRank(String name, boolean applyOnAdd) {
        Testing.ranksGenerator
                .getParsed(this, name)
                .ifPresent(po -> this.ladderRank = po.getIndex());
    }

    @Override
    public void setLadderRank(int index, boolean applyOnAdd) {
        Testing.ranksGenerator
                .getParsed(this, index)
                .ifPresent(po -> this.ladderRank = po.getIndex());
    }

    public void _setLadderRank(int index) {
        this.ladderRank = index;
    }

    @Override
    public void setPrestige(int index, boolean applyOnAdd) {
        Testing.prestigeGenerator
                .getParsed(this, index)
                .ifPresent(po -> this.prestige = po.getIndex());
    }

    public void _setPrestige(int index) {
        this.prestige = index;
    }

    @Override
    public LadderObject getParsedLadderRank() {
        return Testing.ranksGenerator
                .getParsed(this, ladderRank)
                .orElseThrow(() -> new IllegalStateException("Failed to find ladder rank by index: " + ladderRank));
    }

    @Override
    public Optional<LadderObject> getParsedPrestige() {
        return Optional.ofNullable(prestige <= 0 ? null : Testing.prestigeGenerator
                .getParsed(this, prestige)
                .orElseThrow(() -> new IllegalStateException("Failed to find prestige by index: " + prestige)));
    }

    @Override
    public BigDecimal getPrice(ItemStack itemStack) {
        BigDecimal bigDecimal = pricesCache.get(itemStack);
        if (bigDecimal != null) {
            SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("[Prisoner Price] {}'s Found cached price of {}: {}", getOfflinePlayer().getName(), itemStack, bigDecimal.toString());
            return bigDecimal;
        }

        final BigDecimal[] price = new BigDecimal[]{new BigDecimal(0)};
        if (SuperiorPrisonPlugin.getInstance().getMainConfig().isUseMineShopsByRank()) {
            price[0] = getMines().stream()
                    .filter(mine -> mine.getAccess().canEnter(this))
                    .map(mine -> mine.getShop().getPrice(itemStack))
                    .findFirst()
                    .orElse(new BigDecimal(0));

        } else
            for (SuperiorMine mine : getMines()) {
                SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("[Prisoner Price]: {}'s Checking {} mine shop", getOfflinePlayer().getName(), mine.getName());
                BigDecimal minePrice = mine.getShop().getPrice(itemStack);
                if (minePrice.compareTo(price[0]) > 0) {
                    SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("[Prisoner Price] {}'s Using price from {}: {}", getOfflinePlayer().getName(), mine.getName(), minePrice.toString());
                    price[0] = minePrice;
                }
            }

        if (price[0].doubleValue() == 0 && SuperiorPrisonPlugin.getInstance().getMainConfig().isShopGuiAsFallBack())
            SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> ShopGuiPlusHook.class, hook -> price[0] = BigDecimal.valueOf(hook.getPriceFor(itemStack, getPlayer())));

        getBoosters().findBoostersBy(MoneyBooster.class).forEach(booster -> price[0] = price[0] = price[0].multiply(BigDecimal.valueOf(booster.getRate())));
        bigDecimal = price[0];
        pricesCache.put(itemStack, bigDecimal);

        SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("[Prisoner Price] {}'s Final price of {} is {}", getOfflinePlayer().getName(), itemStack, price[0]);
        return bigDecimal;
    }

    private void updateHighestMine() {
//        highestMine = SuperiorPrisonPlugin.getInstance().getDatabaseController().getMineHolder()
//                .getMinesFor(this)
//                .stream()
//                .filter(mine -> {
//                    boolean hasRank = mine.hasRank(getCurrentLadderRank().getName());
//                    boolean hasPrestige = true;
//                    if (!mine.getPrestiges().isEmpty()) {
//                        if (getCurrentPrestige().isPresent()) {
//                            return mine.hasPrestige(getCurrentPrestige().get().getName());
//                        } else
//                            hasPrestige = false;
//                    }
//                    return hasPrestige && hasRank;
//                })
//                .findFirst()
//                .orElse(SuperiorPrisonPlugin.getInstance().getDatabaseController().getMineHolder().getMinesFor(this).stream().findFirst().orElse(null));
    }

    @Override
    public SuperiorMine getHighestMine() {
        if (highestMine == null)
            updateHighestMine();

        return highestMine;
    }

    @Override
    public String getTable() {
        return "prisoners";
    }

    @Override
    public String getKey() {
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
                "boosters",
                "currentPrestige",
                "logoutmine",
                "currentLadderRank",
                "texture"
        };
    }

    @Override
    public void remove() {
        if (removed) return;
        removed = true;

        SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SPrisonerHolder.class).remove(this);
        getCurrentMine().ifPresent(pair -> pair.getKey().getPrisoners().remove(this));

        SuperiorPrisonPlugin.getInstance().getStatisticsController().getIfFound(getUUID()).ifPresent(SStatisticsContainer::remove);
    }

    @Override
    public void serialize(SerializedData data) {
        data.write("uuid", uuid);
        data.write("autoburn", autoBurn);
        data.write("autopickup", autoPickup);
        data.write("autosell", autoSell);
        data.write("fortuneblocks", fortuneBlocks);
        data.write("currentLadderRank", ladderRank);
        data.write("currentPrestige", prestige);
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

        this.logoutMine = data.getElement("logoutmine").map(jsonElement -> jsonElement.isJsonNull() ? null : jsonElement).map(JsonElement::getAsString).orElse(null);
        this.boosters = data.applyAs("boosters", SBoosters.class);
        this.boosters.attach(this);

        if (data.has("currentLadderRank")) {
            String currentLadderRank = data.getChildren("currentLadderRank").get().applyAs(String.class);
            ladderRank = Values.isNumber(currentLadderRank)
                    ? Values.parseAsInt(currentLadderRank)
                    : Testing.ranksGenerator.getParsed(this, currentLadderRank)
                    .orElseThrow(() -> new IllegalStateException("Failed to find rank by " + currentLadderRank))
                    .getIndex();
        }

        data.getChildren("currentPrestige").ifPresent(currentPrestige -> {
            if (currentPrestige.applyAs(int.class) != 0)
                this.prestige = Testing.prestigeGenerator
                        .isValid(currentPrestige.applyAs(int.class))
                        ? currentPrestige.applyAs(int.class)
                        : 0;
        });

        ensurePlayerNotNull();
        this.textureValue = data.getElement("texture").map(JsonElement::getAsString).orElse(getOnlineSkullTexture());
    }

    @SneakyThrows
    private String getOnlineSkullTexture() {
        if (!isOnline()) return defaultHeadTexture;

        Object entityPlayer = OSimpleReflection.getMethod(getPlayer().getClass(), "getHandle").invoke(getPlayer());
        GameProfile profile = (GameProfile) Objects.requireNonNull(OSimpleReflection.getMethod(entityPlayer.getClass(), "getProfile"), "GameProfile field is null").invoke(entityPlayer);
        return profile.getProperties().get("textures").stream().findFirst().map(Property::getValue).orElse(null);
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

    @Override
    public String getSerializedType() {
        return "prisoner";
    }

    public String getTextureValue() {
        if (textureValue == null) return defaultHeadTexture;
        if (textureValue.contentEquals(defaultHeadTexture) && isOnline())
            textureValue = getOnlineSkullTexture();

        return textureValue;
    }

    public Optional<OPair<Integer, SBackPack>> getOpenedBackpack() {
        return Optional.ofNullable(openedBackpack);
    }

    public void unlockBackpack() {
        openedBackpack = null;
    }

    public void lockBackpack(int slot, SBackPack backPack) {
        openedBackpack = new OPair<>(slot, backPack);
    }
}
