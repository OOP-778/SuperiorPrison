package com.bgsoftware.superiorprison.plugin.config.backpack;
import com.bgsoftware.superiorprison.plugin.object.backpack.BackPackData;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.ConfigValue;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

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

    public BackPackConfig(ConfigSection section) {
        this.id = section.getKey();
        applyUpgrades(section);

        section.getSection("upgrades")
                .ifPresent(upgradesSection -> {
                    BackPackConfig lastClone = clone();
                    for (ConfigSection upgradeSection : upgradesSection.getSections().values()) {
                        BackPackConfig clone = lastClone.clone();
                        clone.applyUpgrades(upgradeSection);
                        upgrades.put(Integer.parseInt(upgradeSection.getKey()), new BackPackUpgrade(upgradeSection, clone));

                        lastClone = clone;
                    }
                });
    }

    public void applyUpgrades(ConfigSection section) {
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

    public BackPackConfig getByLevel(int level) {
        return level == 1 ? this : Objects.requireNonNull(upgrades.get(level), "Failed to find BackPack " + id + " level by " + level).getConfig();
    }

    public BackPackConfig getByData(BackPackData data) {
        return data.level == 1 ? this : Objects.requireNonNull(upgrades.get(data.level), "Failed to find BackPack " + id + " level by " + data.level).getConfig();
    }
}
