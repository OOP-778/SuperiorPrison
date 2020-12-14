package com.bgsoftware.superiorprison.plugin.block;

import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class XpHandler {
    public static Map<Integer, Supplier<Integer>> xpHandler = new HashMap<>();

    static {
        register(OMaterial.REDSTONE_ORE, () -> random(1, 5));
        register(OMaterial.LAPIS_ORE, () -> random(2, 5));
        register(OMaterial.COAL_ORE, () -> random(0, 2));
        register(OMaterial.DIAMOND_ORE, () -> random(3, 7));
        register(OMaterial.EMERALD_ORE, () -> random(3, 7));
        register(OMaterial.NETHER_QUARTZ_ORE, () -> random(2, 5));
    }

    private static void register(OMaterial oMaterial, Supplier<Integer> supplier) {
        xpHandler.put(oMaterial.getCombinedId(), supplier);
    }

    private static int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(max - min + 1) + min;
    }

    public static int getEXP(OMaterial material, ItemStack itemStack) {
        if (itemStack != null) {
            OItem item = new OItem(itemStack);
            if (item.hasEnchant(Enchantment.SILK_TOUCH)) return 0;
        }

        return Optional.ofNullable(xpHandler.get(material.getCombinedId())).map(Supplier::get).orElse(0);
    }

    public static Supplier<Integer> getSupplier(OMaterial material) {
        return Optional.ofNullable(xpHandler.get(material.getCombinedId())).orElse(() -> 0);
    }
}
