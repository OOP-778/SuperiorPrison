package com.bgsoftware.superiorprison.plugin.block;

import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class DropsHandler {
    private static final HashMap<Integer, Supplier<ItemStack>> drops = new HashMap<>();

    static {
        register(OMaterial.DIAMOND_ORE, () -> new ItemStack(Material.DIAMOND));
        register(OMaterial.EMERALD_ORE, () -> new ItemStack(Material.EMERALD));
        register(OMaterial.COAL_ORE, () -> new ItemStack(Material.COAL, 1));
        register(OMaterial.LAPIS_ORE, () -> OMaterial.LAPIS_LAZULI.parseItem(ThreadLocalRandom.current().nextInt(4, 9)));
        register(OMaterial.REDSTONE_ORE, () -> new ItemStack(Material.REDSTONE, 5));
        register(OMaterial.NETHER_QUARTZ_ORE, () -> new ItemStack(Material.QUARTZ, 1));
        register(OMaterial.STONE, () -> new ItemStack(Material.COBBLESTONE, 1));
    }

    private static void register(OMaterial oMaterial, Supplier<ItemStack> supplier) {
        drops.put(oMaterial.getCombinedData(), supplier);
    }

    public static ItemStack getDrop(OMaterial material) {
        ItemStack itemStack = Optional.ofNullable(drops.get(material.getCombinedData())).map(Supplier::get).orElse(material.parseItem());
        return itemStack;
    }
}