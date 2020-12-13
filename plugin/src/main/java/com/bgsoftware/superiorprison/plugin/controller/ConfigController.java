package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.economy.EconomyConfig;
import com.bgsoftware.superiorprison.plugin.config.main.MainConfig;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.holders.SEconomyHolder;
import com.bgsoftware.superiorprison.plugin.util.menu.updater.MenuUpdater;
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

    private final Map<String, Config> menus = Maps.newHashMap();
    private Config localeConfig;
    private Config ranksConfig;
    private Config prestigesConfig;
    private Config chatConfig;
    private Config backPacksConfig;
    private Config bombsConfig;
    private Config economyConfig;

    public ConfigController() {}

    @Override
    public boolean load() {
        menus.clear();
        try {
            File dataFolder = getPlugin().getDataFolder();

            OFile localeFile = new OFile(dataFolder, "locale.yml").createIfNotExists();
            OFile ranksFile = new OFile(dataFolder, "ranks.yml").createIfNotExists(true);
            OFile prestigesFile = new OFile(dataFolder, "prestiges.yml").createIfNotExists(true);
            OFile chatFile = new OFile(dataFolder, "chat.yml").createIfNotExists(true);
            OFile backpacksFile = new OFile(dataFolder, "backpacks.yml").createIfNotExists(true);
            OFile bombsFile = new OFile(dataFolder, "bombs.yml").createIfNotExists(true);
            OFile economyFile = new OFile(dataFolder, "economy.yml").createIfNotExists(true);

            this.chatConfig = new Config(chatFile);
            this.localeConfig = new Config(localeFile);
            this.ranksConfig = new Config(ranksFile);
            this.prestigesConfig = new Config(prestigesFile);
            this.backPacksConfig = new Config(backpacksFile);
            this.bombsConfig = new Config(bombsFile);
            this.economyConfig = new Config(economyFile);

            SuperiorPrisonPlugin.getInstance().setMainConfig(new MainConfig());

            if (SuperiorPrisonPlugin.getInstance().getDatabaseController() != null)
                ((SEconomyHolder) SuperiorPrisonPlugin.getInstance().getEconomyController()).setConfig(new EconomyConfig(new Config(economyFile)));

            SuperiorPrisonPlugin.getInstance().setPrestigeController(new LadderObjectController(prestigesConfig, true));
            SuperiorPrisonPlugin.getInstance().setRankController(new LadderObjectController(ranksConfig, false));

            JarUtil.copyFolderFromJar("menus", dataFolder, JarUtil.CopyOption.COPY_IF_NOT_EXIST, SuperiorPrisonPlugin.class);

            // Update
            int updated = MenuUpdater.update("menus");
            if (updated != 0)
                getPlugin().getOLogger().print("Updated {} menu objects!", updated);

            for (File menuFile : Objects.requireNonNull(new File(dataFolder + "/menus").listFiles(File::isFile)))
                this.menus.put(menuFile.getName().replace(".yml", "").toLowerCase(), new Config(menuFile));

            Locale.load(SuperiorPrisonPlugin.getInstance().getMainConfig().getLocale());
            LocaleEnum.load();
        } catch (Throwable thrw) {
            throw new IllegalStateException("Failed to load ConfigController", thrw);
        }

        return true;
    }
}
