package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.controller.BlockController;
import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.data.player.booster.DropsBooster;
import com.bgsoftware.superiorprison.api.data.player.booster.MoneyBooster;
import com.bgsoftware.superiorprison.api.event.mine.MultiBlockBreakEvent;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.drops.DropsHandler;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.object.mine.SMineBlockData;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.SPair;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SBlockController implements BlockController {
    @Override
    public MultiBlockBreakEvent syncHandleBlockBreak(Prisoner prisoner, SuperiorMine mine, ItemStack tool, Location... locations) {
        boolean silkTouch = false;
        boolean hasFortune = false;
        int fortuneLevel = -1;
        OItem item;

        if (tool != null) {
            item = new OItem(tool);
            silkTouch = item.hasEnchant(Enchantment.SILK_TOUCH);
            hasFortune = item.hasEnchant(Enchantment.LOOT_BONUS_BLOCKS);
            fortuneLevel = item.getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
        }

        Set<DropsBooster> boosters = prisoner.getBoosters().findBoostersBy(DropsBooster.class);
        double[] rate = new double[]{0};
        boosters.forEach(booster -> rate[0] = rate[0] + booster.getRate());

        // Get the drops
        Map<Location, Pair<Material, List<ItemStack>>> blockData = new HashMap<>();

        boolean finalSilkTouch = silkTouch;
        boolean finalHasFortune = hasFortune;
        int finalFortuneLevel = fortuneLevel;

        for (Location location : locations) {
            ((SMineBlockData) mine.getGenerator().getBlockData())
                    .getOMaterialAt(location)
                    .ifPresent(mat -> {
                        List<ItemStack> drops = new ArrayList<>();

                        // Handle auto burn
                        if (prisoner.isAutoBurn()) {
                            if (mat == OMaterial.GOLD_ORE)
                                drops.add(OMaterial.GOLD_INGOT.parseItem());
                            else if (mat == OMaterial.IRON_ORE)
                                drops.add(OMaterial.IRON_INGOT.parseItem());
                        } else
                            drops.add(finalSilkTouch ? mat.parseItem() : DropsHandler.getDrop(mat));

                        // Handle fortune
                        if (finalHasFortune)
                            drops.forEach(itemStack -> {
                                if ((itemStack.getType().isBlock() && prisoner.isFortuneBlocks()) || !itemStack.getType().isBlock())
                                    itemStack.setAmount(getItemCountWithFortune(itemStack.getType(), finalFortuneLevel));
                            });

                        // Handle boosters
                        if (rate[0] != 0)
                            drops.forEach(itemStack -> itemStack.setAmount((int) Math.round(rate[0] * itemStack.getAmount())));

                        blockData.put(location, new SPair<>(
                                mat.parseMaterial(),
                                drops));
                    });
        }

        // Call block break event for the blocks
        MultiBlockBreakEvent event = new MultiBlockBreakEvent(mine, prisoner, tool, blockData);
        Bukkit.getPluginManager().callEvent(event);

        // Handle auto selling
        if (prisoner.isAutoSell()) {
            BigDecimal[] deposit = new BigDecimal[]{new BigDecimal(0)};

            // Get prices of the drops
            blockData.values()
                    .forEach(c -> {
                        c.getValue().removeIf(itemStack -> {
                            BigDecimal price = prisoner.getPrice(itemStack);
                            if (price.intValue() == 0) return false;

                            deposit[0] = deposit[0].add(price);
                            return true;
                        });
                    });

            // Apply money boosters
            prisoner.getBoosters()
                    .findBoostersBy(MoneyBooster.class)
                    .forEach(booster -> deposit[0] = deposit[0].multiply(BigDecimal.valueOf(booster.getRate())));

            // Finally deposit
            SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> VaultHook.class, hook -> hook.depositPlayer((SPrisoner) prisoner, deposit[0]));
        }

        // Handle auto pickup
        if (prisoner.isAutoPickup()) {
            blockData.values()
                    .forEach(d -> {
                        HashMap<Integer, ItemStack> left = prisoner.getPlayer().getInventory().addItem(d.getValue().toArray(new ItemStack[0]));
                        d.getValue().clear();

                        d.getValue().addAll(left.values());
                    });
        }

        return event;
    }

    private int getItemCountWithFortune(Material material, int enchant_level) {
        int drops = ThreadLocalRandom.current().nextInt(enchant_level + 2) - 1;
        if (drops < 0)
            drops = 0;

        int i = material == Material.LAPIS_BLOCK ? 4 + ThreadLocalRandom.current().nextInt(5) : 1;
        return i * (drops + 1);
    }
}
