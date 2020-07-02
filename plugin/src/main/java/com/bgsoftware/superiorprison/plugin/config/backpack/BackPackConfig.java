package com.bgsoftware.superiorprison.plugin.config.backpack;
import com.bgsoftware.superiorprison.plugin.object.backpack.BackPackData;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.oop.orangeengine.item.ItemStackUtil;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.ConfigValue;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiConsumer;

import static com.oop.orangeengine.main.Engine.getEngine;

public class BackPackConfig implements Cloneable {

    private static Map<String, BiConsumer<BackPackConfig, Object>> upgradeHandlers = new HashMap<>();
    static {
        registerUpgrade("item", ConfigSection.class, (backpack, section) -> backpack.item = new OItem().load(section));
        registerUpgrade("rows", int.class, (backpack, rows) -> backpack.rows = rows);
        registerUpgrade("pages", int.class, (backpack, pages) -> backpack.pages = pages);
    }

    private static <T extends Object> void registerUpgrade(String path, Class<T> type, BiConsumer<BackPackConfig, T> consumer) {
        upgradeHandlers.put(path, (BiConsumer<BackPackConfig, Object>) consumer);
    }

    private Map<Integer, BackPackUpgrade> upgrades = new HashMap<>();

    @Getter
    private OItem item;

    @Getter
    private int rows;

    @Getter
    private int pages;

    @Getter
    private String id;

    @Getter
    private int level = 1;

    public BackPackConfig(ConfigSection section) {
        this.id = section.getKey();
        applyUpgrades(section);

        section.getSection("upgrades")
                .ifPresent(upgradesSection -> {
                    BackPackConfig lastClone = clone();
                    for (ConfigSection upgradeSection : upgradesSection.getSections().values()) {
                        BackPackConfig clone = lastClone.clone();
                        clone.applyUpgrades(upgradeSection);
                        clone.level = Integer.parseInt(upgradeSection.getKey());
                        upgrades.put(clone.level, new BackPackUpgrade(upgradeSection, clone));

                        lastClone = clone;
                    }
                });
    }

    private void applyUpgrades(ConfigSection section) {
        upgradeHandlers.forEach((key, consumer) -> {
            Optional<ConfigSection> optSection = section.getSection(key);
            Optional<ConfigValue> optValue = section.get(key);

            if (optSection.isPresent()) {
                consumer.accept(this, optSection.get());
            } else
                optValue.ifPresent(configValue -> consumer.accept(this, configValue.getObject()));
        });
    }

    @SneakyThrows
    public BackPackConfig clone() {
        BackPackConfig clone = (BackPackConfig) super.clone();
        clone.item = clone.item.clone();
        return clone;
    }

    public BackPackUpgrade getUpgrade(int level) {
        return level == 1 ? null : Objects.requireNonNull(upgrades.get(level), "Failed to find BackPack " + id + " level by " + level);
    }

    public BackPackConfig getByLevel(int level) {
        return level == 1 ? this : Objects.requireNonNull(upgrades.get(level), "Failed to find BackPack " + id + " level by " + level).getConfig();
    }

    public BackPackConfig getByData(BackPackData data) {
        return data.level == 1 ? this : Objects.requireNonNull(upgrades.get(data.level), "Failed to find BackPack " + id + " level by " + data.level).getConfig();
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
}
