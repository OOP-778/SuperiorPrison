package com.bgsoftware.superiorprison.plugin.config.backpack;

import com.bgsoftware.superiorprison.plugin.object.backpack.BackPackData;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.ConfigValue;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class BackPackConfig<T extends BackPackConfig<T>> {
    protected static Map<String, BiConsumer<BackPackConfig, Object>> upgradeHandlers = new HashMap<>();

    static {
        registerUpgrade("item", BackPackConfig.class, ConfigSection.class, (backpack, section) -> backpack.item = ItemBuilder.fromConfiguration(section));
    }

    private Map<Integer, BackPackUpgrade<T>> upgrades = new HashMap<>();
    @Getter
    private ItemBuilder item;
    @Getter
    private String id;
    @Getter
    private int level = 1;

    protected BackPackConfig() {
    }

    public BackPackConfig(ConfigSection section) {
        this.id = section.getKey();
        applyUpgrades(section);

        section.getSection("upgrades")
                .ifPresent(upgradesSection -> {
                    T lastClone = clone();
                    for (ConfigSection upgradeSection : upgradesSection.getSections().values()) {
                        T clone = lastClone.clone();
                        clone.applyUpgrades(upgradeSection);

                        ((BackPackConfig) clone).level = Integer.parseInt(upgradeSection.getKey());
                        upgrades.put(((BackPackConfig) clone).level, new BackPackUpgrade<>(upgradeSection, clone));

                        lastClone = clone;
                    }
                });
    }

    protected static <B extends BackPackConfig, T extends Object> void registerUpgrade(String path, Class<B> backpackClass, Class<T> type, BiConsumer<B, T> consumer) {
        upgradeHandlers.put(path, (BiConsumer<BackPackConfig, Object>) consumer);
    }

    public abstract T clone();

    protected void applyUpgrades(ConfigSection section) {
        upgradeHandlers.forEach((key, consumer) -> {
            Optional<ConfigSection> optSection = section.getSection(key);
            Optional<ConfigValue> optValue = section.get(key);

            if (optSection.isPresent()) {
                consumer.accept(this, optSection.get());
            } else
                optValue.ifPresent(configValue -> consumer.accept(this, configValue.getObject()));
        });
    }

    public BackPackUpgrade<T> getUpgrade(int level) {
        return level == 1 ? null : Objects.requireNonNull(upgrades.get(level), "Failed to find BackPack " + id + " level by " + level);
    }

    public T getByLevel(int level) {
        return level == 1 ? (T) this : Objects.requireNonNull(upgrades.get(level), "Failed to find BackPack " + id + " level by " + level).getConfig();
    }

    public T getByData(BackPackData data) {
        return data.getLevel() == 1 ? (T) this : Objects.requireNonNull(upgrades.get(data.getLevel()), "Failed to find BackPack " + id + " level by " + data.getLevel()).getConfig();
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

    public abstract int getCapacity();

    protected void superClone(BackPackConfig backPackConfig) {
        backPackConfig.id = id;
        backPackConfig.item = item;
        backPackConfig.level = level;
        backPackConfig.upgrades = upgrades;
    }
}
