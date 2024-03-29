package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.MineEnum;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.mine.locks.Lock;
import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.data.player.rank.SpecialRank;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.main.MineDefaultsSection;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.mine.effects.SMineEffects;
import com.bgsoftware.superiorprison.plugin.object.mine.linkable.LinkableObject;
import com.bgsoftware.superiorprison.plugin.object.mine.linkable.ObjectLinker;
import com.bgsoftware.superiorprison.plugin.object.mine.locks.SMineLock;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineMessages;
import com.bgsoftware.superiorprison.plugin.object.mine.reward.SMineRewards;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;
import com.bgsoftware.superiorprison.plugin.object.mine.shop.SShop;
import com.bgsoftware.superiorprison.plugin.object.player.SPrestige;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SSpecialRank;
import com.bgsoftware.superiorprison.plugin.util.AccessUtil;
import com.bgsoftware.superiorprison.plugin.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
import com.google.common.collect.Maps;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.util.DataUtil;
import com.oop.datamodule.lib.google.gson.JsonArray;
import com.oop.datamodule.lib.google.gson.JsonElement;
import com.oop.datamodule.lib.google.gson.JsonObject;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.cache.OCache;
import com.oop.orangeengine.main.util.data.set.OConcurrentSet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SNormalMine
    implements com.bgsoftware.superiorprison.api.data.mine.type.NormalMine,
        Serializable,
        UniversalBodyModel {
  private final Set<Prisoner> prisoners = ConcurrentHashMap.newKeySet();

  private final Set<String> ranks = new OConcurrentSet<>();

  private final Set<String> prestiges = new OConcurrentSet<>();
  private final Set<SpecialRank> specialRanks = new HashSet<>();
  private final Map<String, LinkableObject> linkableObjectMap = new HashMap<>();
  private final OCache<UUID, Boolean> canEnterCache =
      OCache.builder().concurrencyLevel(1).expireAfter(5, TimeUnit.SECONDS).build();
  @Setter private String name;
  private MineEnum type = MineEnum.NORMAL_MINE;
  @Setter private SPLocation spawnPoint = null;
  private SMineGenerator generator;
  @Setter private SShop shop;
  @Getter @Setter private SMineSettings settings;
  @Setter private ItemStack icon;
  @Getter private Map<AreaEnum, SArea> areas = Maps.newConcurrentMap();
  @Getter private SMineEffects effects;
  @Getter private SMineMessages messages;
  private SPrestige highestPrestige;
  private SLadderRank highestRank;
  @Getter private SMineRewards rewards;
  @Getter private ObjectLinker linker = new ObjectLinker();

  @Getter
  private OCache<Lock, Boolean> pendingTasks =
      OCache.builder().concurrencyLevel(1).expireAfter(5, TimeUnit.SECONDS).build();

  private SNormalMine() {}

  public SNormalMine(
      @NonNull String name,
      @NonNull SPLocation regionPos1,
      @NonNull SPLocation regionPos2,
      @NonNull SPLocation minePos1,
      @NonNull SPLocation minePos2,
      @NonNull SPLocation spawnPoint) {
    this.name = name;
    this.spawnPoint = spawnPoint;
    this.areas.put(AreaEnum.MINE, new SArea(minePos1, minePos2, AreaEnum.MINE));
    this.areas.put(AreaEnum.REGION, new SArea(regionPos1.y(0), regionPos2.y(255), AreaEnum.REGION));
    areas.values().forEach(area -> area.attach(this));
    this.shop = new SShop();
    shop.attach(this);

    checkForPrisoners();

    MineDefaultsSection defaults =
        SuperiorPrisonPlugin.getInstance().getMainConfig().getMineDefaults();
    this.icon =
        ItemBuilder.fromItem(defaults.getIcon().getItemStack().clone())
            .replaceDisplayName("{mine_name}", name)
            .getItemStack();

    this.settings = new SMineSettings(defaults);
    settings.attach(this);

    generator = new SMineGenerator();
    defaults.getMaterials().forEach(material -> generator.getGeneratorMaterials().add(material));
    generator.setMine(this);
    generator.setMineArea(getArea(AreaEnum.MINE));

    defaults
        .getShopPrices()
        .forEach(item -> shop.addItem(item.getFirst().parseItem(), item.getSecond()));
    StaticTask.getInstance().async(() -> generator.initCache(() -> generator.generate(null)));
    settings.setPlayerLimit(defaults.getLimit());

    effects = new SMineEffects();
    effects.attach(this);

    messages = new SMineMessages();
    messages.attach(this);

    linker.attach(this);

    generator.reset();
    updateHighests();

    this.rewards = new SMineRewards();
    initializeLinkableObjects();
  }

  public Map<String, LinkableObject> getLinkableObjects() {
    return linkableObjectMap;
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

    if (mine.isInsideWithoutY(location)) return mine;

    if (region.isInsideWithoutY(location)) return region;

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
    if (areas.get(AreaEnum.MINE).isInsideWithoutY(location)) return AreaEnum.MINE;

    if (areas.get(AreaEnum.REGION).isInsideWithoutY(location)) return AreaEnum.REGION;

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
  public Set<String> getRanks() {
    return new HashSet<>(ranks);
  }

  @Override
  public Set<Rank> getRanksMapped() {
    return ranks.stream()
        .map(
            name ->
                SuperiorPrisonPlugin.getInstance().getRankController().getRank(name).orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<String> getPrestiges() {
    return new HashSet<>(prestiges);
  }

  @Override
  public Set<Prestige> getPrestigesMapped() {
    return prestiges.stream()
        .map(
            name ->
                SuperiorPrisonPlugin.getInstance()
                    .getPrestigeController()
                    .getPrestige(name)
                    .orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  @Override
  public ItemStack getIcon() {
    return icon;
  }

  @Override
  public boolean canEnter(Prisoner prisoner) {
    Boolean canEnter = canEnterCache.get(prisoner.getUUID());
    if (canEnter != null) return canEnter;

    boolean hasRanks = true;
    if (highestRank != null)
      hasRanks = prisoner.getCurrentLadderRank().getOrder() >= highestRank.getOrder();

    boolean hasPrestiges = true;
    if (highestPrestige != null)
      hasPrestiges =
          prisoner.getCurrentPrestige().isPresent()
              && prisoner.getCurrentPrestige().get().getOrder() >= highestPrestige.getOrder();

    canEnter =
        prisoner.getPlayer().hasPermission("superiorprison.bypass") || (hasPrestiges && hasRanks);
    canEnterCache.put(prisoner.getUUID(), canEnter);

    return canEnter;
  }

  @Override
  public void save(boolean async) {
    save(async, null);
  }

  @Override
  public void removeRank(String... rank) {
    Arrays.stream(rank).forEach(ranks::remove);
  }

  @Override
  public void removeRank(Rank... rank) {
    Arrays.stream(rank).map(Rank::getName).forEach(ranks::remove);
  }

  @Override
  public void removePrestige(String... prestige) {
    Arrays.stream(prestige).forEach(prestiges::remove);
  }

  @Override
  public void removePrestige(Prestige... prestige) {
    Arrays.stream(prestige).map(Prestige::getName).forEach(prestiges::remove);
  }

  @Override
  public void addRank(String... rank) {
    Arrays.stream(rank)
        .map(AccessUtil::findRank)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(Rank::getName)
        .forEach(ranks::add);
  }

  @Override
  public void addRank(Rank... rank) {
    Arrays.stream(rank).map(Rank::getName).forEach(ranks::add);
  }

  @Override
  public void addPrestige(String... prestige) {
    Arrays.stream(prestige)
        .map(AccessUtil::findPrestige)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(Prestige::getName)
        .forEach(prestiges::add);
  }

  @Override
  public void addPrestige(Prestige... prestige) {
    Arrays.stream(prestige).map(Prestige::getName).forEach(prestiges::add);
  }

  protected void onReset(Runnable callback) {
    getPrisoners()
        .removeIf(
            prisoner -> {
              boolean online = false;
              if (!prisoner.isOnline()) {
                ((SPrisoner) prisoner).setLogoutMine(getName());
                prisoner.save(true);
                online = true;
              }
              return online || !prisoner.getCurrentMine().isPresent();
            });

    StaticTask.getInstance()
        .sync(
            () -> {
              getPrisoners().stream()
                  .filter(prisoner -> prisoner.getCurrentMine().get().getValue() == AreaEnum.MINE)
                  .filter(prisoner -> prisoner.getPlayer().getLocation().getY() < getArea(AreaEnum.MINE).getHighPoint().getY())
                  .forEach(
                      prisoner -> {
                        Framework.FRAMEWORK.teleport(prisoner.getPlayer(), getSpawnPoint());
                        LocaleEnum.MINE_RESETTING.getWithPrefix().send(prisoner.getPlayer());
                      });

              if (callback != null)
                callback.run();
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
  public String getKey() {
    return name;
  }

  @Override
  public String[] getStructure() {
    return new String[] {
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
      "rewards"
    };
  }

  @Override
  public void serialize(SerializedData serializedData) {
    serializedData.write("name", name);
    serializedData.write("type", type.name());
    serializedData.write("spawnpoint", spawnPoint);
    serializedData.write("generator", generator);
    serializedData.write("shop", shop);
    serializedData.write("ranks", ranks);
    serializedData.write("prestiges", prestiges);
    serializedData.write("settings", settings);
    serializedData.write("icon", icon);
    serializedData.write("effects", effects);
    serializedData.write("messages", messages);
    serializedData.write("linker", linker);
    serializedData.write("rewards", rewards);

    JsonArray areasArray = new JsonArray();
    areas.forEach(
        (key, value) -> {
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
    this.ranks.addAll(
        data.applyAsCollection("ranks")
            .map(sd -> sd.applyAs(String.class))
            .collect(Collectors.toSet()));
    this.prestiges.addAll(
        data.applyAsCollection("prestiges")
            .map(sd -> sd.applyAs(String.class))
            .collect(Collectors.toSet()));
    this.settings = data.applyAs("settings", SMineSettings.class);
    this.effects =
        data.has("effects") ? data.applyAs("effects", SMineEffects.class) : new SMineEffects();
    this.messages =
        data.has("messages") ? data.applyAs("messages", SMineMessages.class) : new SMineMessages();
    JsonArray areasArray = data.getElement("areas").get().getAsJsonArray();
    for (JsonElement element : areasArray) {
      JsonObject jsonObject = element.getAsJsonObject();
      JsonElement key = jsonObject.get("key");
      JsonElement value = jsonObject.get("value");

      areas.put(AreaEnum.valueOf(key.getAsString()), DataUtil.fromElement(value, SArea.class));
    }
    this.icon = data.applyAs("icon", ItemStack.class);
    this.rewards =
        data.has("rewards") ? data.applyAs("rewards", SMineRewards.class) : new SMineRewards();

    generator.attach(this);
    shop.attach(this);
    settings.attach(this);
    effects.attach(this);
    messages.attach(this);

    Map<AreaEnum, SArea> newAreas = Maps.newConcurrentMap();
    areas.keySet().stream()
        .sorted(Comparator.comparingInt(AreaEnum::getOrder))
        .sorted(Comparator.reverseOrder())
        .forEachOrdered(areaEnum -> newAreas.put(areaEnum, areas.get(areaEnum)));

    this.areas = newAreas;
    areas.values().forEach(area -> area.attach(this));

    if (getSettings().getResetSettings().isTimed()) getGenerator().reset();

    if (data.has("linker")) linker = data.applyAs("linker", ObjectLinker.class);

    linker.attach(this);
    updateHighests();

    initializeLinkableObjects();
  }

  private void initializeLinkableObjects() {
    getLinkableObjects().put("effects", effects);
    getLinkableObjects().put("settings", settings);
    getLinkableObjects().put("shop", shop);
    getLinkableObjects().put("generator", generator);
    getLinkableObjects().put("messages", messages);
    getLinkableObjects().put("rewards", rewards);
  }

  public void remove() {
    SuperiorPrisonPlugin.getInstance()
        .getDatabaseController()
        .getStorage(SMineHolder.class)
        .remove(this);
  }

  @Override
  public void save(boolean b, Runnable runnable) {
    SuperiorPrisonPlugin.getInstance()
        .getDatabaseController()
        .getStorage(SMineHolder.class)
        .save(this, b, runnable);
  }

  public void checkForPrisoners() {
    Helper.getOnlinePlayers().stream()
        .filter(
            player ->
                player.getLocation().getWorld().getName().equalsIgnoreCase(getWorld().getName()))
        .filter(player -> getArea(AreaEnum.REGION).isInsideWithoutY(player.getLocation()))
        .forEach(
            player ->
                prisoners.add(
                    SuperiorPrisonPlugin.getInstance()
                        .getPrisonerController()
                        .getInsertIfAbsent(player)));
  }

  public void updateHighests() {
    StaticTask.getInstance()
        .ensureAsync(
            () -> {
              canEnterCache.clear();

              // Find highest prestige
              highestPrestige =
                  (SPrestige)
                      prestiges.stream()
                          .map(
                              prestigeName ->
                                  SuperiorPrisonPlugin.getInstance()
                                      .getPrestigeController()
                                      .getPrestige(prestigeName)
                                      .orElse(null))
                          .filter(Objects::nonNull)
                          .max(Comparator.comparingInt(Prestige::getOrder))
                          .orElse(null);

              // Initialize ranks
              specialRanks.clear();
              List<LadderRank> ladderRanks = new ArrayList<>();
              for (String rankName : ranks) {
                Rank rank =
                    SuperiorPrisonPlugin.getInstance()
                        .getRankController()
                        .getRank(rankName)
                        .orElse(null);
                if (rank == null) continue;

                if (rank instanceof LadderRank) ladderRanks.add((LadderRank) rank);
                else specialRanks.add((SSpecialRank) rank);
              }

              highestRank =
                  (SLadderRank)
                      ladderRanks.stream()
                          .max(Comparator.comparingInt(LadderRank::getOrder))
                          .orElse(null);
            });
  }

  @Override
  public boolean hasRank(String name) {
    return ranks.contains(name);
  }

  @Override
  public boolean hasPrestige(String name) {
    return prestiges.contains(name);
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
    Location location =
        new Location(
            player.getWorld(),
            player.getLocation().getBlockX(),
            player.getLocation().getBlockY(),
            player.getLocation().getBlockZ());
    location.add(0.5, 0.5, 0.5);

    location.setPitch(player.getEyeLocation().getPitch());
    location.setYaw(player.getEyeLocation().getYaw());
    setSpawnPoint(new SPLocation(location));
  }

  @Override
  public String getIdentifierKey() {
    return "name";
  }
}
