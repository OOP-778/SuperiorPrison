package com.bgsoftware.superiorprison.plugin.controller;

import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.yaml.OConfiguration;
import lombok.Getter;

import java.io.File;

import static com.oop.orangeengine.main.Engine.getEngine;

@Getter
public class ConfigController {

    private OConfiguration menusConfig;
    private OConfiguration localeConfig;
    private OConfiguration minesRewardsConfig;
    private OConfiguration ranksConfig;
    private OConfiguration prestigesConfig;

    public ConfigController() {
        File dataFolder = getEngine().getOwning().getDataFolder();
        if (!dataFolder.exists())
            dataFolder.mkdirs();

        OFile menusFile = new OFile(dataFolder, "menus.yml").createIfNotExists(true);
        OFile localeFile = new OFile(dataFolder, "locale.yml").createIfNotExists();
        OFile mineRewardsFile = new OFile(dataFolder, "mineRewards.yml").createIfNotExists(true);
        OFile ranksFile = new OFile(dataFolder, "rankups.yml").createIfNotExists(true);

        this.menusConfig = new OConfiguration(menusFile);
        this.localeConfig = new OConfiguration(localeFile);
        this.minesRewardsConfig = new OConfiguration(mineRewardsFile);
        this.ranksConfig = new OConfiguration(ranksFile);

        menusConfig.clearDefaultHeader();
        menusConfig.appendHeader("Here you can edit / create your menus!");
        menusConfig.appendHeader("Make sure you don't change default names of menus otherwise menus might not work as!");
        menusConfig.save();
    }
}
