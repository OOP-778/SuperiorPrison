package com.bgsoftware.superiorprison.plugin.controller;

import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.menu.config.ConfigMenuTemplate;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.oop.orangeengine.main.Engine.getEngine;

public class ConfigController {

    public ConfigController() {
        File dataFolder = getEngine().getOwning().getDataFolder();
        if (!dataFolder.exists())
            dataFolder.mkdirs();

        OFile configFile = new OFile(dataFolder, "config.yml").createIfNotExists(true);
        OFile menusFile = new OFile(dataFolder, "menus.yml").createIfNotExists(true);

        OConfiguration mainConfig = new OConfiguration(configFile);
        OConfiguration menusConfig = new OConfiguration(menusFile);
        menusConfig.clearDefaultHeader();
        menusConfig.appendHeader("Here you can edit / create your menus!");
        menusConfig.appendHeader("Make sure you don't change default names of menus otherwise menus might not work as!");
        menusConfig.save();

        for (ConfigurationSection section : menusConfig.getSections().values())
            if (!section.getKey().contentEquals("global buttons"))
                loadedMenuTemplates.put(section.getKey(), new ConfigMenuTemplate(section));
    }

    private Map<String, ConfigMenuTemplate> loadedMenuTemplates = new HashMap<>();

    public Optional<ConfigMenuTemplate> findMenuTemplate(String name) {
        return Optional.ofNullable(loadedMenuTemplates.get(name));
    }

}
