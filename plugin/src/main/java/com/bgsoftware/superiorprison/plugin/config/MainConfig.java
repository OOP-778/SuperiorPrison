package com.bgsoftware.superiorprison.plugin.config;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.yaml.Config;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public class MainConfig {

    public Config configuration;
    private String locale = "en-us";

    private boolean shopGuiAsFallBack = false;

    private long cacheTime = TimeUnit.HOURS.toMillis(1);
    private DatabaseSection database;
    private MineDefaultsSection mineDefaults;
    private CommandColorsSection commandColors;

    private OItem areaSelectionTool;

    public MainConfig() {
        load();
    }

    private void load() {
        this.configuration = new Config(new OFile(SuperiorPrisonPlugin.getInstance().getDataFolder(), "config.yml").createIfNotExists(true));

        // Set Locale
        configuration.ifValuePresent("locale", String.class, locale -> this.locale = locale);

        // Load Database Section
        this.database = new DatabaseSection(configuration.getSection("database").get());

        // Load Mine Defaults
        this.mineDefaults = new MineDefaultsSection(configuration.createSection("mine defaults"));

        this.areaSelectionTool = new OItem().load(configuration.getSection("area selection tool").get());

        configuration.ifValuePresent("shopgui fall back", boolean.class, b -> shopGuiAsFallBack = b);
        configuration.ifSectionPresent("command colors", section -> commandColors = new CommandColorsSection(section));

        cacheTime = TimeUtil.toSeconds(configuration.getAs("blocks cache time limit", String.class, () -> "1h"));
        configuration.save();
    }
}
