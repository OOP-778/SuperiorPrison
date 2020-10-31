package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.MineEnum;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.mine.locks.Lock;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.main.MineDefaultsSection;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.object.mine.access.SMineAccess;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.mine.effects.SMineEffects;
import com.bgsoftware.superiorprison.plugin.object.mine.linkable.LinkableObject;
import com.bgsoftware.superiorprison.plugin.object.mine.linkable.ObjectLinker;
import com.bgsoftware.superiorprison.plugin.object.mine.locks.SMineLock;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineMessages;
import com.bgsoftware.superiorprison.plugin.object.mine.reward.SMineRewards;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;
import com.bgsoftware.superiorprison.plugin.object.mine.shop.SShop;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.Testing;
import com.bgsoftware.superiorprison.plugin.util.Removeable;
import com.bgsoftware.superiorprison.plugin.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
import com.google.common.collect.Maps;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.body.MultiTypeBody;
import com.oop.datamodule.gson.JsonArray;
import com.oop.datamodule.gson.JsonElement;
import com.oop.datamodule.gson.JsonObject;
import com.oop.datamodule.util.DataUtil;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.cache.OCache;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SNormalMine implements com.bgsoftware.superiorprison.api.data.mine.type.NormalMine, Serializable, MultiTypeBody, Removeable {
    private final Set<Prisoner> prisoners = ConcurrentHashMap.newKeySet();

    @Setter
    private String name;
    private MineEnum type = MineEnum.NORMAL_MINE;
    @Setter
    private SPLocation spawnPoint = null;
    private SMineGenerator generator;
    @Setter
    private SShop shop;
    @Getter
    @Setter
    private SMineSettings settings;

    @Setter
    private ItemStack icon;

    @Getter
    private Map<AreaEnum, SArea> areas = Maps.newConcurrentMap();

    @Getter
    private SMineEffects effects;

    @Getter
    private SMineMessages messages;

    @Getter
    private SMineRewards rewards;

    @Getter
    private ObjectLinker linker = new ObjectLinker();

    @Getter
    private SMineAccess access;

    private final Map<String, LinkableObject> linkableObjectMap = new HashMap<>();

    @Getter
    private OCache<Lock, Boolean> pendingTasks = OCache
            .builder()
            .concurrencyLevel(1)
            .expireAfter(5, TimeUnit.SECONDS)
            .build();

    @Getter
    @Setter
    private boolean removed = false;
    private final OCache<UUID, Boolean> canEnterCache = OCache
            .builder()
            .concurrencyLevel(1)
            .expireAfter(5, TimeUnit.SECONDS)
            .build();

    private SNormalMine() {
    }

    public SNormalMine(@NonNull String name, @NonNull SPLocation regionPos1, @NonNull SPLocation regionPos2, @NonNull SPLocation minePos1, @NonNull SPLocation minePos2, @NonNull SPLocation spawnPoint) {
        this.name = name;
        this.spawnPoint = spawnPoint;
        this.areas.put(AreaEnum.MINE, new SArea(minePos1, minePos2, AreaEnum.MINE));
        this.areas.put(AreaEnum.REGION, new SArea(regionPos1.y(0), regionPos2.y(255), AreaEnum.REGION));
        areas.values().forEach(area -> area.attach(this));
        this.shop = new SShop();
        shop.attach(this);

        checkForPrisoners();

        MineDefaultsSection defaults = SuperiorPrisonPlugin.getInstance().getMainConfig().getMineDefaults();
        this.icon = ItemBuilder.fromItem(defaults.getIcon().getItemStack().clone())
                .replaceDisplayName("{mine_name}", name)
                .getItemStack();

        this.settings = new SMineSettings(defaults);
        settings.attach(this);

        generator = new SMineGenerator();
        defaults.getMaterials().forEach(material -> generator.getGeneratorMaterials().add(material));
        generator.setMine(this);
        generator.setMineArea(getArea(AreaEnum.MINE));

        defaults.getShopPrices().forEach(item -> shop.addItem(item.getFirst().parseItem(), item.getSecond()));
        StaticTask.getInstance().async(() -> generator.initCache(() -> generator.generate()));
        settings.setPlayerLimit(defaults.getLimit());

        effects = new SMineEffects();
        effects.attach(this);

        messages = new SMineMessages();
        messages.attach(this);

        linker.attach(this);

        generator.reset();

        access = new SMineAccess();
        access.attach(this);

        this.rewards = new SMineRewards();
        initializeLinkableObjects();
    }

    @Override
    public MineEnum getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SArea getArea(AreaEnum type) {
        return areas.get(type);
    }

    @Override
    public Area getArea(Location location) {
        Area mine = getArea(AreaEnum.MINE);
        Area region = getArea(AreaEnum.REGION);

        if (mine.isInsideWithoutY(location))
            return mine;

        if (region.isInsideWithoutY(location))
            return region;

        return null;
    }

    @Override
    public Location getSpawnPoint() {
        return spawnPoint.toBukkit();
    }

    @Override
    public SMineGenerator getGenerator() {
        return generator;
    }

    @Override
    public int getPlayerCount() {
        return prisoners.size();
    }

    @Override
    public Set<Prisoner> getPrisoners() {
        return prisoners;
    }

    @Override
    public boolean isInside(Location location) {
        return areas.get(AreaEnum.REGION).isInsideWithoutY(location);
    }

    @Nullable
    @Override
    public AreaEnum getAreaTypeAt(Location location) {
        if (areas.get(AreaEnum.MINE).isInsideWithoutY(location))
            return AreaEnum.MINE;

        if (areas.get(AreaEnum.REGION).isInsideWithoutY(location))
            return AreaEnum.REGION;

        return null;
    }

    @Override
    public boolean isInsideArea(AreaEnum areaEnum, Location location) {
        return getArea(areaEnum).isInsideWithoutY(location);
    }

    @Override
    public World getWorld() {
        return getArea(AreaEnum.MINE).getHighPoint().getWorld();
    }

    @Override
    public SShop getShop() {
        return shop;
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    @Deprecated
    public boolean canEnter(Prisoner prisoner) {
        if (prisoner.getPlayer().isOp()) return true;
//        Boolean canEnter = canEnterCache.get(prisoner.getUUID());
//        if (canEnter != null) return canEnter;
//
//        boolean hasRanks = true;
//        if (highestRank != null)
//            hasRanks = prisoner.getCurrentLadderRank().getOrder() >= highestRank.getOrder();
//
//        boolean hasPrestiges = true;
//        if (highestPrestige != null)
//            hasPrestiges = prisoner.getCurrentPrestige().isPresent() && prisoner.getCurrentPrestige().get().getOrder() >= highestPrestige.getOrder();
//
//        canEnter = prisoner.getPlayer().hasPermission("superiorprison.bypass") || (hasPrestiges && hasRanks);
//        canEnterCache.put(prisoner.getUUID(), canEnter);

        return access.canEnter(prisoner);
    }

    @Override
    public void save(boolean async) {
        save(async, null);
    }

    public void onReset() {
        getPrisoners().removeIf(prisoner -> {
            boolean online = false;
            if (!prisoner.isOnline()) {
                ((SPrisoner) prisoner).setLogoutMine(getName());
                prisoner.save(true);
                online = true;
            }
            return online || !prisoner.getCurrentMine().isPresent();
        });

        StaticTask.getInstance().sync(() -> {
            getPrisoners().stream().filter(prisoner -> prisoner.getCurrentMine().get().getValue() == AreaEnum.MINE).forEach(prisoner -> {
                Framework.FRAMEWORK.teleport(prisoner.getPlayer(), getSpawnPoint());
                LocaleEnum.MINE_RESETTING.getWithPrefix().send(prisoner.getPlayer());
            });
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SNormalMine that = (SNormalMine) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String getTable() {
        return "mines";
    }

    @Override
    public String getKey() {
        return name;
    }

    @Override
    public String getSerializedType() {
        return "normalMine";
    }

    @Override
    public String[] getStructure() {
        return new String[]{
                "name",
                "type",
                "spawnpoint",
                "generator",
                "shop",
                "ranks",
                "prestiges",
                "settings",
                "icon",
                "areas",
                "effects",
                "messages",
                "linker",
                "rewards",
                "access"
        };
    }

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("name", name);
        serializedData.write("type", type.name());
        serializedData.write("spawnpoint", spawnPoint);
        serializedData.write("generator", generator);
        serializedData.write("shop", shop);
        serializedData.write("settings", settings);
        serializedData.write("icon", icon);
        serializedData.write("effects", effects);
        serializedData.write("messages", messages);
        serializedData.write("linker", linker);
        serializedData.write("rewards", rewards);

        // Removed in 3 versions
        serializedData.write("ranks", null);
        serializedData.write("prestiges", null);

        serializedData.write("access", access);

        JsonArray areasArray = new JsonArray();
        areas.forEach((key, value) -> {
            JsonObject object = new JsonObject();
            object.addProperty("key", key.name());

            SerializedData data = new SerializedData();
            value.serialize(data);

            object.add("value", data.getJsonElement());
            areasArray.add(object);
        });
        serializedData.getJsonElement().getAsJsonObject().add("areas", areasArray);
    }

    @Override
    public void deserialize(SerializedData data) {
        this.name = data.applyAs("name", String.class);
        this.type = MineEnum.valueOf(data.applyAs("type", String.class));
        this.spawnPoint = data.applyAs("spawnpoint", SPLocation.class);
        this.generator = data.applyAs("generator", SMineGenerator.class);
        this.shop = data.applyAs("shop", SShop.class);
        this.settings = data.applyAs("settings", SMineSettings.class);
        this.effects = data.has("effects") ? data.applyAs("effects", SMineEffects.class) : new SMineEffects();
        this.messages = data.has("messages") ? data.applyAs("messages", SMineMessages.class) : new SMineMessages();
        JsonArray areasArray = data.getElement("areas").get().getAsJsonArray();
        for (JsonElement element : areasArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonElement key = jsonObject.get("key");
            JsonElement value = jsonObject.get("value");

            areas.put(AreaEnum.valueOf(key.getAsString()), DataUtil.fromElement(value, SArea.class));
        }

        this.icon = data.applyAs("icon", ItemStack.class);
        this.rewards = data.has("rewards") ? data.applyAs("rewards", SMineRewards.class) : new SMineRewards();

        generator.attach(this);
        shop.attach(this);
        settings.attach(this);
        effects.attach(this);
        messages.attach(this);

        Map<AreaEnum, SArea> newAreas = Maps.newConcurrentMap();
        areas.keySet()
                .stream()
                .sorted(Comparator.comparingInt(AreaEnum::getOrder))
                .sorted(Comparator.reverseOrder())
                .forEachOrdered(areaEnum -> newAreas.put(areaEnum, areas.get(areaEnum)));

        this.areas = newAreas;
        areas.values().forEach(area -> area.attach(this));

        if (getSettings().getResetSettings().isTimed())
            getGenerator().reset();

        if (data.has("linker"))
            linker = data.applyAs("linker", ObjectLinker.class);

        linker.attach(this);
        this.access = data.getChildren("access").map(sd -> sd.applyAs(SMineAccess.class)).orElse(new SMineAccess());
        access.attach(this);

        // Migrate old data (FUCKING HARD)
        data
                .getChildren("prestiges")
                .ifPresent(sd -> migrateLadder(sd, false));

        data
                .getChildren("ranks")
                .ifPresent(sd -> migrateLadder(sd, true));
        initializeLinkableObjects();
    }

    private void migrateLadder(SerializedData data, boolean isRank) {
        String getter = isRank ? "%prisoner#ladderrank%" : "%prisoner#prestige%";
        String name = isRank ? "Rank Check" : "Prestige Check";

        data.applyAsCollection()
                .map(sd -> sd.applyAs(String.class))
                .mapToInt(sd -> Testing.prestigeGenerator.getIndex(sd))
                .filter(index -> index != -1)
                .max()
                .ifPresent(index -> {
                    access
                            .addScript(
                                    name,
                                    "if {" + getter + " == -1}: false else: { " + getter +  " >= " + index + "}"
                            );
                });
    }

    private void initializeLinkableObjects() {
        getLinkableObjects().put("effects", effects);
        getLinkableObjects().put("settings", settings);
        getLinkableObjects().put("shop", shop);
        getLinkableObjects().put("generator", generator);
        getLinkableObjects().put("messages", messages);
        getLinkableObjects().put("rewards", rewards);
        getLinkableObjects().put("access", access);
    }

    @Override
    public void remove() {
        removed = true;
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SMineHolder.class).remove(this);
    }

    @Override
    public void save(boolean b, Runnable runnable) {
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getStorage(SMineHolder.class).save(this, b, runnable);
    }

    public void checkForPrisoners() {
        Helper.getOnlinePlayers()
                .stream()
                .filter(player -> player.getLocation().getWorld().getName().equalsIgnoreCase(getWorld().getName()))
                .filter(player -> getArea(AreaEnum.REGION).isInsideWithoutY(player.getLocation()))
                .forEach(player -> prisoners.add(SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player)));
    }

    public void updateHighests() {
        StaticTask.getInstance().ensureAsync(() -> {
            canEnterCache.clear();

//            // Find highest prestige
//            highestPrestige = (SPrestige) prestiges
//                    .stream()
//                    .map(prestigeName -> SuperiorPrisonPlugin.getInstance().getPrestigeController().getPrestige(prestigeName).orElse(null))
//                    .filter(Objects::nonNull)
//                    .max(Comparator.comparingInt(Prestige::getOrder))
//                    .orElse(null);
//
//            // Initialize ranks
//            specialRanks.clear();
//            List<LadderRank> ladderRanks = new ArrayList<>();
//            for (String rankName : ranks) {
//                Rank rank = SuperiorPrisonPlugin.getInstance().getRankController().getRank(rankName).orElse(null);
//                if (rank == null) continue;
//
//                if (rank instanceof LadderRank)
//                    ladderRanks.add((LadderRank) rank);
//                else
//                    specialRanks.add((SSpecialRank) rank);
//            }
//
//            highestRank = (SLadderRank) ladderRanks
//                    .stream()
//                    .max(Comparator.comparingInt(LadderRank::getOrder))
//                    .orElse(null);
        });
    }

    public Map<String, LinkableObject> getLinkableObjects() {
        return linkableObjectMap;
    }

    @Override
    public boolean isReady() {
        return !generator.isCaching() && !generator.isResetting() && !generator.isWorldLoadWait();
    }

    @Override
    public Lock newLock() {
        SMineLock lock = new SMineLock();
        pendingTasks.put(lock, true);
        return lock;
    }

    @Override
    public void unlock(Lock lock) {
        pendingTasks.remove(lock);
    }

    public void clean() {
        generator.clean();
    }

    public void setSpawnPointOf(Player player) {
        Location location = new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        location.add(0.5, 0.5, 0.5);

        location.setPitch(player.getEyeLocation().getPitch());
        location.setYaw(player.getEyeLocation().getYaw());
        setSpawnPoint(new SPLocation(location));
    }
}
