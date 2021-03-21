package com.bgsoftware.superiorprison.plugin.config.backpack;

import com.bgsoftware.superiorprison.plugin.object.backpack.BackPackData;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.ConfigValue;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.Getter;
import org.bukkit.entity.Player;

public class BackPackConfig {
  protected static Map<OPair<String, Class<?>>, BiConsumer<BackPackConfig, Object>> upgradeHandlers =
      new HashMap<>();

  static {
    registerUpgrade(
        "item",
        BackPackConfig.class,
        ConfigSection.class,
        (backpack, section) -> backpack.item = ItemBuilder.fromConfiguration(section));

    registerUpgrade(
            "capacity",
            BackPackConfig.class,
            String.class,
            (back, i) -> {
              back.capacity = new BigInteger(i);
            });
  }

  private Map<Integer, BackPackUpgrade> upgrades = new HashMap<>();
  @Getter private ItemBuilder item;
  @Getter private String id;
  @Getter private int level = 1;
  @Getter private BigInteger capacity;
  @Getter private String type;

  @Getter private boolean sellByDefault = false;

  protected BackPackConfig() {}

  public BackPackConfig(ConfigSection section) {
    this.id = section.getKey();
    applyUpgrades(section);

    // Make sure values are migrated
    if (section.getAs("type", String.class).equalsIgnoreCase("advanced")) {
      for (ConfigSection value : section.getHierarchySections().values()) {
        migrateAdvancedBackPackSection(value);
      }
    } else
      section.set("type", "advanced");

    this.type = section.getAs("type");

    section.ifValuePresent("sellable", boolean.class, b -> this.sellByDefault = b);

    section
        .getSection("upgrades")
        .ifPresent(
            upgradesSection -> {
              BackPackConfig lastClone = clone();
              for (ConfigSection upgradeSection : upgradesSection.getSections().values()) {
                BackPackConfig clone = lastClone.clone();
                clone.applyUpgrades(upgradeSection);

                clone.level = Integer.parseInt(upgradeSection.getKey());
                upgrades.put(
                    clone.level, new BackPackUpgrade(upgradeSection, clone));

                lastClone = clone;
              }
            });
  }

  protected void migrateAdvancedBackPackSection(ConfigSection section) {
    if (!section.isValuePresent("pages") || !section.isValuePresent("rows")) return;

    int pages = section.getAs("pages", int.class);
    int rows = section.getAs("rows", int.class);
    section.getValues().remove("pages");
    section.getValues().remove("rows");

    section.set("capacity", rows * 9 * pages * 64);
  }

  protected static <B extends BackPackConfig, T extends Object> void registerUpgrade(
      String path, Class<B> backpackClass, Class<T> type, BiConsumer<B, T> consumer) {
    upgradeHandlers.put(new OPair<>(path, type), (BiConsumer<BackPackConfig, Object>) consumer);
  }


  protected void applyUpgrades(ConfigSection section) {
    upgradeHandlers.forEach(
        (key, consumer) -> {
          Optional<ConfigSection> optSection = section.getSection(key.getKey());
          Optional<?> optValue = section.get(key.getKey(), key.getValue());

          if (optSection.isPresent()) {
            consumer.accept(this, optSection.get());
          } else optValue.ifPresent(configValue -> consumer.accept(this, optValue.get()));
        });
  }

  public BackPackUpgrade getUpgrade(int level) {
    return level == 1
        ? null
        : Objects.requireNonNull(
            upgrades.get(level), "Failed to find BackPack " + id + " level by " + level);
  }

  public BackPackConfig getByLevel(int level) {
    return level == 1
        ? this
        : Objects.requireNonNull(
                upgrades.get(level), "Failed to find BackPack " + id + " level by " + level)
            .getConfig();
  }

  public BackPackConfig getByData(BackPackData data) {
    return data.getLevel() == 1
        ? this
        : Objects.requireNonNull(
                upgrades.get(data.getLevel()),
                "Failed to find BackPack " + id + " level by " + data.getLevel())
            .getConfig();
  }

  public int getMaxLevel() {
    return upgrades.keySet().stream().mapToInt(integer -> integer).max().orElse(1);
  }

  public SBackPack build(Player player) {
    return SBackPack.of(this, player);
  }

  public boolean hasUpgrade() {
    return upgrades.containsKey(level + 1);
  }

  @Override
  protected BackPackConfig clone() {
    BackPackConfig clone = new BackPackConfig();
    clone.id = id;
    clone.item = item;
    clone.level = level;
    clone.capacity = capacity;
    clone.upgrades = upgrades;
    clone.type = type;
    return clone;
  }
}
