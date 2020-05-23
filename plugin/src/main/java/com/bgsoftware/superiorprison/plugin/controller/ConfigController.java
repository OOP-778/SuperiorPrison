package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.MainConfig;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.google.common.collect.Maps;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.plugin.OComponent;
import com.oop.orangeengine.main.util.JarUtil;
import com.oop.orangeengine.message.locale.Locale;
import com.oop.orangeengine.yaml.Config;
import lombok.Getter;

import java.io.File;
import java.util.Map;
import java.util.Objects;

@Getter
public class ConfigController implements OComponent<SuperiorPrisonPlugin> {

    private Config localeConfig;
    private Config minesRewardsConfig;
    private Config ranksConfig;
    private Config prestigesConfig;
    private final Map<String, Config> menus = Maps.newHashMap();

    public ConfigController() {
    }

    @Override
    public boolean load() {
        menus.clear();
        try {
            File dataFolder = getPlugin().getDataFolder();

            OFile localeFile = new OFile(dataFolder, "locale.yml").createIfNotExists();
            OFile ranksFile = new OFile(dataFolder, "ranks.yml").createIfNotExists(true);
            OFile prestigesFile = new OFile(dataFolder, "prestiges.yml").createIfNotExists(true);

            this.localeConfig = new Config(localeFile);
            this.ranksConfig = new Config(ranksFile);
            this.prestigesConfig = new Config(prestigesFile);

            JarUtil.copyFolderFromJar("menus", dataFolder, JarUtil.CopyOption.COPY_IF_NOT_EXIST, SuperiorPrisonPlugin.class);
            for (File menuFile : Objects.requireNonNull(new File(dataFolder + "/menus").listFiles(File::isFile)))
                menus.put(menuFile.getName().replace(".yml", "").toLowerCase(), new Config(menuFile));

            SuperiorPrisonPlugin.getInstance().setMainConfig(new MainConfig());
            Locale.load(SuperiorPrisonPlugin.getInstance().getMainConfig().getLocale());
            LocaleEnum.load();
        } catch (Throwable thrw) {
            throw new IllegalStateException("Failed to load ConfigController", thrw);
        }

        return true;
    }
}
