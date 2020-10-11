package com.bgsoftware.superiorprison.plugin.object.inventory;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.SBackPackController;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.nbt.NBTItem;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.bgsoftware.superiorprison.plugin.controller.SBackPackController.UUID_KEY;
import static com.bgsoftware.superiorprison.plugin.util.ItemStackUtil.isNamed;
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
        SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Add item");
        // If for some reason the inventory is not patched
        if (!(player.getInventory() instanceof PatchedInventory)) return itemStacks;

        // If auto pickup is disabled return
        if (!prisoner.isAutoPickup()) return itemStacks;
        SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Prisoner has enabled Auto Pickup");

        // If prisoner is not in a mine return
        if (!prisoner.getCurrentMine().isPresent()) return itemStacks;
        SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Prisoner is in mine");

        ItemStack[] itemStacks1 = Arrays.copyOfRange(itemStacks, 0, itemStacks.length);

        // Clean out named items if config says so
        if (!SuperiorPrisonPlugin.getInstance().getMainConfig().isHandleNamedItems()) {
            for (int i = 0; i < itemStacks.length; i++) {
                ItemStack itemStack = itemStacks[i];
                if (itemStack == null) continue;

                if (isNamed(itemStack)) {
                    SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Removing {} from the adding items.", itemStack);
                    itemStacks1[i] = null;
                }
            }
        }

        // If the item stacks are empty, return
        SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Given ItemStacks: {}", Arrays.toString(itemStacks));
        if (Arrays.stream(itemStacks1).noneMatch(Objects::nonNull)) return itemStacks;

        SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Given itemstacks are not empty :)");

        for (SBackPack backpack : backPackMap.values()) {
            // If backpack is full, ignore
            if (backpack.isFull()) continue;

            // Try to add the items
            Map<ItemStack, Integer> add = backpack.add(itemStacks1);
            SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Add left: " + add.size());

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

            if (getBPC().isBackPack(itemStack)) {
                SBackPack backPack = (SBackPack) getBPC().getBackPack(itemStack, player);
                backPackMap.put(i, backPack);
            }
        }
        SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Found {} backpacks in {} inventory", backPackMap.size(), player.getName());
    }

    public ItemStack[] removeItem(ItemStack... itemStacks) {
        // If for some reason the inventory is not patched.
        if (!(player.getInventory() instanceof PatchedInventory)) return itemStacks;

        for (ItemStack itemStack : itemStacks) {
            SBackPack backPackBy = findBackPackBy(itemStack);
            if (backPackBy == null) continue;

            int slotByBackPack = backPackBy.getCurrentSlot();
            backPackMap.remove(slotByBackPack);
        }

        return itemStacks;
    }

    public ItemStack setItem(int slot, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            SBackPack sBackPack = backPackMap.get(slot);
            if (sBackPack != null)
                backPackMap.remove(slot);
            return itemStack;
        }

        if (getBPC().isBackPack(itemStack)) {
            // Check if the itemstack had last location
            SBackPack currentBackpack = backPackMap.get(slot);

            System.out.println("had backpack?: " + (currentBackpack != null));
            UUID uuid = SuperiorPrisonPlugin.getInstance().getBackPackController().getUUID(itemStack);
            if (uuid != null && currentBackpack != null && uuid.equals(currentBackpack.getUuid())) return itemStack;

            else {
                SBackPack pack = (SBackPack) SuperiorPrisonPlugin.getInstance().getBackPackController().getBackPack(itemStack, player);
                System.out.println("new backpack:" + pack.getUsed());
                backPackMap.put(slot, pack);
            }

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

    @SneakyThrows
    public SBackPack findBackPackBy(ItemStack itemStack) {
        if (!SuperiorPrisonPlugin.getInstance().getBackPackController().isBackPack(itemStack)) return null;

        NBTItem nbtItem = new NBTItem(itemStack);
        String serializedUUID = nbtItem.getString(UUID_KEY);
        if (serializedUUID == null || serializedUUID.equalsIgnoreCase("")) return null;

        UUID uuid = UUID.fromString(serializedUUID);

        return backPackMap.values()
                .stream()
                .filter(b -> b.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public int getSlotByBackPack(SBackPack backPack) {
        return getBackPackMap().entrySet()
                .stream()
                .filter(es -> es.getValue().equals(backPack))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);
    }
}
