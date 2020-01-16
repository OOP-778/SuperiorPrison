package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.google.common.collect.Maps;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.plugin.OComponent;
import com.oop.orangeengine.main.util.JarUtil;
import com.oop.orangeengine.yaml.OConfiguration;
import com.oop.orangeengine.yaml.updater.ConfigurationUpdater;
import lombok.Getter;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static com.oop.orangeengine.main.Engine.getEngine;

@Getter
public class ConfigController implements OComponent<SuperiorPrisonPlugin> {

    private OConfiguration menusConfig;
    private OConfiguration localeConfig;
    private OConfiguration minesRewardsConfig;
    private OConfiguration ranksConfig;
    private OConfiguration prestigesConfig;
    private Map<String, OConfiguration> menus = Maps.newHashMap();

    public ConfigController() {}

    @Override
    public boolean load() {
        menus.clear();
        try {
            File dataFolder = getPlugin().getDataFolder();

            OFile menusFile = new OFile(dataFolder, "menus.yml").createIfNotExists(true);
            OFile localeFile = new OFile(dataFolder, "locale.yml").createIfNotExists();
            OFile mineRewardsFile = new OFile(dataFolder, "mineRewards.yml").createIfNotExists(true);
            OFile ranksFile = new OFile(dataFolder, "ranks.yml").createIfNotExists(true);

            this.menusConfig = new OConfiguration(menusFile);
            ConfigurationUpdater updater = menusConfig.updater();
            int updated = updater.update();

            if (updated > 0) {
                getEngine().getLogger().print("Updated menus.yml (" + updated + ") values!");
            }

            this.localeConfig = new OConfiguration(localeFile);
            this.minesRewardsConfig = new OConfiguration(mineRewardsFile);
            this.ranksConfig = new OConfiguration(ranksFile);

            menusConfig.clearDefaultHeader();
            menusConfig.appendHeader("Here you can edit / create your menus!");
            menusConfig.appendHeader("Make sure you don't change default names of menus otherwise menus might not work as!");
            menusConfig.save();

            JarUtil.copyFolderFromJar("menus", dataFolder, JarUtil.CopyOption.COPY_IF_NOT_EXIST, SuperiorPrisonPlugin.class);
            for (File menuFile : Objects.requireNonNull(new File(dataFolder + "/menus").listFiles(File::isFile)))
                menus.put(menuFile.getName().replace(".yml", "").toLowerCase(), new OConfiguration(menuFile));

            for (String s : menus.keySet()) {
                System.out.println(s);
            }

        } catch (Throwable thrw) {
            getPlugin().getOLogger().error(thrw);
            return false;
        }

        return true;
    }
}
