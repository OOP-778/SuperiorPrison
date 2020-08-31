package com.bgsoftware.superiorprison.plugin.config.backpack;

import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;
import lombok.SneakyThrows;

public class AdvancedBackPackConfig extends BackPackConfig<AdvancedBackPackConfig> implements Cloneable {
    static {
        registerUpgrade("rows", AdvancedBackPackConfig.class, int.class, (backpack, rows) -> backpack.rows = rows);
        registerUpgrade("pages", AdvancedBackPackConfig.class, int.class, (backpack, pages) -> backpack.pages = pages);
    }

    @Getter
    private int rows;

    @Getter
    private int pages;

    public AdvancedBackPackConfig(ConfigSection section) {
        super(section);
    }

    public AdvancedBackPackConfig() {}

    @SneakyThrows
    public AdvancedBackPackConfig clone() {
        AdvancedBackPackConfig clone = new AdvancedBackPackConfig();
        superClone(clone);
        clone.pages = pages;
        clone.rows = rows;
        return clone;
    }
}
