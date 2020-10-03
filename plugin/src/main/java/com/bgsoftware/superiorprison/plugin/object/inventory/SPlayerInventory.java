package com.bgsoftware.superiorprison.plugin.object.inventory;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.SBackPackController;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.main.util.OSimpleReflection;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.bukkit.Bukkit.getServer;

public class SPlayerInventory {
    private final Player player;
    private SPrisoner prisoner;

    @Getter
    private Map<Integer, SBackPack> backPackMap = new ConcurrentHashMap<>();

    private static Consumer<Player> patcher;

    static {
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            Class<?> patchedInventoryClass = Class.forName("com.bgsoftware.superiorprison.plugin.nms.PlayerInventory_" + version);
            Class<?> craftPlayerInventoryClass = OSimpleReflection.findClass("{cb}.inventory.CraftInventoryPlayer");
            Class<?> nmsPlayerInventoryClass = OSimpleReflection.findClass("{nms}.PlayerInventory");
            Class<?> craftHumanClass = OSimpleReflection.findClass("{cb}.entity.CraftHumanEntity");

            Field inventoryField = craftHumanClass.getDeclaredField("inventory");
            inventoryField.setAccessible(true);

            Method craftGetNmsInventory = OSimpleReflection.getMethod(craftPlayerInventoryClass, "getInventory");
            Constructor<?> patchedInventoryConstructor = OSimpleReflection.getConstructor(patchedInventoryClass, nmsPlayerInventoryClass, SPlayerInventory.class);

            patcher = (player) -> {
                if (player.getInventory() instanceof PatchedInventory) return;

                try {
                    Object patchedInventory = patchedInventoryConstructor.newInstance(craftGetNmsInventory.invoke(inventoryField.get(player)), new SPlayerInventory(player));
                    inventoryField.set(player, patchedInventory);

                    SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Patched {} inventory!", player.getName());
                } catch (Throwable throwable) {
                    throw new IllegalStateException("Failed to patch player inventory", throwable);
                }
            };

        } catch (Throwable e) {
            throw new IllegalStateException("Unsupported version " + version + ". Failed to find PlayerInventory, contact author!", e);
        }
    }

    public SPlayerInventory(Player player) {
        this.player = player;
        this.prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);

        init();
    }

    public ItemStack[] addItem(ItemStack... itemStacks) {
        // If for some reason the inventory is not patched.
        if (player.getInventory() instanceof PatchedInventory) return itemStacks;

        // If auto pickup is disabled return
        if (!prisoner.isAutoPickup()) return itemStacks;

        // If prisoner is not in a mine return
        if (!prisoner.getCurrentMine().isPresent()) return itemStacks;

        for (SBackPack backpack : backPackMap.values()) {
            // If backpack is full, ignore
            if (backpack.isFull()) continue;

            // Try to add the items
            Map<ItemStack, Integer> add = backpack.add(itemStacks);

            // Added all the items
            if (add.isEmpty())
                return new ItemStack[0];
            else
                itemStacks = add.keySet().toArray(new ItemStack[0]);
        }

        return itemStacks;
    }

    public void init() {
        backPackMap.clear();
        ItemStack contents[] = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;

            if (getBPC().isBackPack(itemStack))
                backPackMap.put(i, (SBackPack) getBPC().getBackPack(itemStack, player));
        }
        SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Found {} backpacks in {} inventory", backPackMap.size(), player.getName());
    }

    public ItemStack[] removeItem(ItemStack... itemStacks) {
        // If for some reason the inventory is not patched.
        if (player.getInventory() instanceof PatchedInventory) return itemStacks;

        return itemStacks;
    }

    public ItemStack setItem(int slot, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return itemStack;

        System.out.println("Set item :)");
        if (getBPC().isBackPack(itemStack)) {
            // Check if the itemstack had last location
            backPackMap.entrySet()
                    .stream()
                    .filter(es -> es.getValue().getItem().equals(itemStack))
                    .findFirst()
                    .ifPresent(es -> backPackMap.remove(es.getKey()));

            backPackMap.put(slot, (SBackPack) getBPC().getBackPack(itemStack, player));
            SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Set backpack slot to " + slot);
        }

        return itemStack;
    }

    public SBackPackController getBPC() {
        return SuperiorPrisonPlugin.getInstance().getBackPackController();
    }

    public static void patch(Player player) {
        patcher.accept(player);
    }

    public SBackPack findBackPackBy(ItemStack itemStack) {
        if (!SuperiorPrisonPlugin.getInstance().getBackPackController().isBackPack(itemStack)) return null;

        SBackPack backPack = (SBackPack) SuperiorPrisonPlugin.getInstance().getBackPackController().getBackPack(itemStack, player);
        return backPackMap.values()
                .stream()
                .filter(b -> b.getData().getUuid().equals(backPack.getData().getUuid()))
                .findFirst()
                .orElse(null);
    }
}
