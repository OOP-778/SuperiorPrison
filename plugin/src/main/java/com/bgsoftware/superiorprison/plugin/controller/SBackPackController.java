package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.controller.BackPackController;
import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.BackPackConfig;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.oop.orangeengine.main.Engine;
import com.oop.orangeengine.main.plugin.OComponent;
import com.oop.orangeengine.nbt.NBTItem;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SBackPackController implements BackPackController, OComponent<SuperiorPrisonPlugin> {

    public static final String NBT_KEY = "BACKPACK_DATA";
    private Map<String, BackPackConfig> backpackConfigs = new HashMap<>();
    private boolean playerBound = false;

    @Override
    public boolean isBackPack(@NonNull ItemStack itemStack) {
        return new NBTItem(itemStack).hasKey(NBT_KEY);
    }

    @Override
    public BackPack getBackPack(@NonNull ItemStack itemStack, Player player) throws IllegalArgumentException {
        try {
            SBackPack backPack = new SBackPack(itemStack, player);
            return backPack;
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to get the backpack", throwable);
        }
    }

    public Optional<BackPackConfig> getConfig(String name) {
        return Optional.ofNullable(backpackConfigs.get(name));
    }

    @Override
    public boolean isPlayerBound() {
        return false;
    }

    @Override
    public Set<BackPack> findBackPacks(Player player) {
        Set<BackPack> backPacks = new HashSet<>();
        for (ItemStack content : player.getInventory().getContents()) {
            if (content == null || content.getType() == Material.AIR) continue;
            if (isBackPack(content)) backPacks.add(getBackPack(content, player));
        }
        return backPacks;
    }

    @Override
    public boolean load() {
        backpackConfigs.clear();
        Config backPacksConfig = SuperiorPrisonPlugin.getInstance().getConfigController().getBackPacksConfig();

        for (ConfigSection section : backPacksConfig.getSections().values()) {
            if (section.getKey().contentEquals("global options")) {
                this.playerBound = section.getAs("player bound");
                continue;
            }

            try {
                backpackConfigs.put(section.getKey(), new BackPackConfig(section));
            } catch (Throwable throwable) {
                getEngine().getLogger().printWarning("Failed to initialize backpack by id {}, check for mistakes!", section.getKey());
            }
        }

        Engine.getInstance().getLogger().print("Loaded {} backpacks", backpackConfigs.size());
        return true;
    }

    public Map<String, BackPackConfig> getConfigs() {
        return backpackConfigs;
    }
}
