package com.bgsoftware.superiorprison.plugin.config.main;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.yaml.OConfiguration;
import lombok.Getter;

@Getter
public class MainConfig {

    public OConfiguration configuration;
    private String locale = "en-us";

    private DatabaseSection database;
    private MineDefaultsSection mineDefaults;

    private OItem areaSelectionTool;

    public MainConfig() {
        load();
    }

    public void reload() {
        load();
    }

    private void load() {
        this.configuration = new OConfiguration(new OFile(SuperiorPrisonPlugin.getInstance().getDataFolder(), "config.yml").createIfNotExists(true));

        // Set Locale
        configuration.ifValuePresent("locale", String.class, locale -> this.locale = locale);

        // Load Database Section
        this.database = new DatabaseSection(configuration.getSection("database"));

        // Load Mine Defaults
        this.mineDefaults = new MineDefaultsSection(configuration.getSection("mine defaults"));

        this.areaSelectionTool = new OItem().load(configuration.getSection("area selection tool"));
    }

}
