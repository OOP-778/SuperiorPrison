package com.bgsoftware.superiorprison.plugin.util.menu.updater;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.util.JarUtil;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class MenuUpdater {
    private static final Map<String, BiFunction<ConfigSection, ConfigSection, Integer>> updateMap = new HashMap<>();

    static {
        updateMap.put("buttons", (updated, old) -> {
            int updatedValues = 0;

            // buttons.value
            for (ConfigSection updatedButton : updated.getSections().values()) {

                Optional<ConfigSection> currentSectionOpt = old.getSection(updatedButton.getKey());
                if (!currentSectionOpt.isPresent()) continue;

                ConfigSection currentButton = currentSectionOpt.get();

                // If states were added
                if (updatedButton.getSections().size() > currentButton.getSections().size()) {
                    // Add sections
                    for (ConfigSection value : updatedButton.getSections().values()) {
                        if (currentButton.isSectionPresent(value.getKey())) continue;

                        ConfigSection newSection = currentButton.createSection(value.getKey());
                        newSection.getValues().putAll(value.getValues());
                        updatedValues += 1;
                    }
                }
            }

            return updatedValues;
        });
    }

    // Update menus at a folderName
    @SneakyThrows
    public static int update(String folderName) {
        AtomicInteger updatedValues = new AtomicInteger();

        File dataFolder = Engine.getEngine().getOwning().getDataFolder();

        // Get the request folder
        File menusFolder = new File(dataFolder + "/" + folderName);
        if (!menusFolder.exists()) return 0;

        File tempFolder = new File(menusFolder + "/temp");
        if (!tempFolder.exists()) tempFolder.mkdirs();

        for (File file : menusFolder.listFiles(file -> file.getName().endsWith("yml"))) {
            try {
                // Copy the file from source
                JarUtil.copyFileFromJar(
                        folderName + "/" + file.getName(),
                        tempFolder,
                        JarUtil.CopyOption.REPLACE_IF_EXIST,
                        file.getName(),
                        SuperiorPrisonPlugin.class
                );
            } catch (Throwable ignored) {}

            // Get the copied file
            File tempFile = new File(tempFolder, file.getName());

            Config tempConfig = new Config(tempFile);
            Config currentConfig = new Config(file);

            updateMap.forEach((path, consumer) -> {
                Optional<ConfigSection> tempSection = tempConfig.getSection(path);
                Optional<ConfigSection> currentSection = currentConfig.getSection(path);
                if (tempSection.isPresent() && currentSection.isPresent()) {
                    Integer apply = consumer.apply(tempSection.get(), currentSection.get());
                    if (apply > 0)
                        currentConfig.save();
                    updatedValues.addAndGet(apply);
                }
            });
            tempFile.delete();
        }
        tempFolder.delete();

        return updatedValues.get();
    }
}
