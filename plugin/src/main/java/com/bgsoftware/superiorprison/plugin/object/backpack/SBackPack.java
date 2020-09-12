package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.AdvancedBackPackConfig;
import com.bgsoftware.superiorprison.plugin.config.backpack.BackPackConfig;
import com.bgsoftware.superiorprison.plugin.config.backpack.SimpleBackPackConfig;
import com.bgsoftware.superiorprison.plugin.menu.backpack.AdvancedBackPackView;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.google.common.base.Preconditions;
import com.oop.datamodule.gson.JsonObject;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.StorageInitializer;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.nbt.NBTItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static com.bgsoftware.superiorprison.plugin.controller.SBackPackController.NBT_KEY;

@AllArgsConstructor
public class SBackPack implements BackPack {

    @Getter
    private BackPackConfig<?> config;

    @Getter
    private BackPackData data;
    private NBTItem nbtItem;
    private ItemStack itemStack;

    @Getter
    private Player owner;

    private JsonObject oldData;

    @Setter
    @Getter
    private AdvancedBackPackView currentView;
    private int hashcode;

    // Advanced Backpack Data
    private int lastRows = -1;
    private int lastPages = -1;

    private SBackPack() {}

    public static SBackPack of(BackPackConfig<?> config, Player player) {
        SBackPack backPack = new SBackPack();
        backPack.config = config;
        backPack.oldData = new JsonObject();
        backPack.owner = player;
        backPack.itemStack = config.getItem().getItemStack().clone();
        backPack.data = new BackPackData(backPack);
        backPack.nbtItem = new NBTItem(backPack.itemStack);

        if (config instanceof AdvancedBackPackConfig) {
            backPack.lastRows = ((AdvancedBackPackConfig) config).getRows();
            backPack.lastPages = ((AdvancedBackPackConfig) config).getPages();
        }

        backPack.updateNbt();
        backPack.save();
        backPack.updateHash();
        return backPack;
    }

    private void updateHash() {
        this.hashcode = data.hashCode();
    }

    @SneakyThrows
    public SBackPack(@Nonnull ItemStack itemStack, Player player) {
        this.owner = player;
        this.itemStack = itemStack;
        this.nbtItem = new NBTItem(itemStack);
        Preconditions.checkArgument(nbtItem.hasKey(NBT_KEY), "The given item is not an backpack");

        String serialized = nbtItem.getString(NBT_KEY);
        oldData = StorageInitializer.getInstance().getGson().fromJson(serialized, JsonObject.class);

        // Check if this is an outdated bool nbt value
        if (SuperiorPrisonPlugin.getInstance().getBackPackController().isPlayerBound()) {
            if (oldData.has("global"))
                oldData = oldData.getAsJsonObject("global");
            else
                oldData = oldData.getAsJsonObject(player.getUniqueId().toString());

        } else if (!SuperiorPrisonPlugin.getInstance().getBackPackController().isPlayerBound()) {
            if (!oldData.has("global"))
                oldData = oldData.getAsJsonObject(player.getUniqueId().toString());
            else
                oldData = oldData.getAsJsonObject("global");
        }

        data = new BackPackData(this);
        if (oldData != null)
            data.deserialize(new SerializedData(oldData));
        else
            oldData = new JsonObject();

        config = SuperiorPrisonPlugin.getInstance().getBackPackController().getConfig(data.getConfigId()).orElseThrow(() -> new IllegalStateException("Failed to find backPack by id " + data.getConfigId() + " level " + data.getLevel())).getByLevel(data.getLevel());
        if (config instanceof AdvancedBackPackConfig) {
            data.updateDataAdvanced(((AdvancedBackPackConfig) config).getRows(), ((AdvancedBackPackConfig) config).getRows(), ((AdvancedBackPackConfig) config).getPages(), ((AdvancedBackPackConfig) config).getPages());
            lastPages = ((AdvancedBackPackConfig) config).getPages();
            lastRows = ((AdvancedBackPackConfig) config).getRows();
        } else
            data.updateData();

        updateHash();
    }

    @Override
    public int getCapacity() {
        return config.getCapacity();
    }

    public int getSlots() {
        return config.getCapacity() / 64;
    }

    @Override
    public int getCurrentLevel() {
        return data.getLevel();
    }

    @Override
    public int getUsed() {
        int used = 0;
        for (int i = 0; i < data.getStored().length; i++) {
            ItemStack itemStack = data.getStored()[i];
            if (itemStack == null) continue;

            used += itemStack.getAmount();
        }

        return used;
    }

    @Override
    public List<ItemStack> getStored() {
        return Arrays.asList(data.getStored());
    }

    @Override
    public String getId() {
        return config.getId();
    }

    public void updateNbt() {
        OItem oItem = config.getItem().clone().makeUnstackable();
        List<String> oldLore = oItem.getLore();
        List<String> newLore = new ArrayList<>();

        for (String line : oldLore) {
            if (line.startsWith("{item_template}")) {
                line = line.replace("{item_template}", "");
                Map<OMaterial, Integer> contents = new HashMap<>();
                for (ItemStack stack : data.getStored()) {
                    if (stack == null || stack.getType() == Material.AIR) continue;
                    OMaterial oMaterial = OMaterial.matchMaterial(stack);
                    contents.putIfAbsent(oMaterial, 0);
                    contents.computeIfPresent(oMaterial, (oldValue, newValue) -> newValue + stack.getAmount());
                }

                String finalLine = line;
                contents.forEach((material, amount) -> {
                    newLore.add(finalLine.replace("{item_type}", TextUtil.beautify(material.name())).replace("{item_amount}", amount + ""));
                });
            } else
                newLore.add(line);
        }

        oItem.setLore(newLore);
        oItem.replace("{backpack_level}", getCurrentLevel());
        oItem.replace("{backpack_capacity}", getCapacity());
        oItem.replace("{backpack_used}", getUsed());
        this.nbtItem = new NBTItem(oItem.getItemStack());
    }

    @Override
    public ItemStack getItem() {
        return nbtItem.getItem();
    }

    @Override
    public void save() {
        if (!isModified()) return;
        updateNbt();
        SerializedData serializedData = new SerializedData();
        data.serialize(serializedData);

        if (SuperiorPrisonPlugin.getInstance().getBackPackController().isPlayerBound()) {
            oldData.remove(owner.getUniqueId().toString());
            oldData.add(owner.getUniqueId().toString(), serializedData.getJsonElement());

        } else {
            oldData.remove("global");
            oldData.add("global", serializedData.getJsonElement());
        }

        nbtItem.setString(NBT_KEY, StorageInitializer.getInstance().getGson().toJson(oldData));
        updateHash();
    }

    @Override
    public Map<ItemStack, Integer> add(ItemStack... itemStacks) {
        Optional<OPair<Integer, ItemStack>> firstNonNull = data.firstNonNull();
        if (!firstNonNull.isPresent() && getCapacity() == getUsed())
            return Arrays.stream(itemStacks).collect(Collectors.toMap(item -> item, item -> 0));

        Map<ItemStack, Integer> addedItems = new HashMap<>();
        for (ItemStack itemStack : itemStacks) {
            int startingAmount = itemStack.getAmount();
            int added = 0;

            while (itemStack.getAmount() != 0) {
                Optional<OPair<Integer, ItemStack>> similar = data.findSimilar(itemStack, true);
                if (similar.isPresent()) {
                    // We have to check if backpack can fit more :)
                    int backpackCanAdd = getCapacity() - getUsed();
                    if (backpackCanAdd <= 0) break;

                    ItemStack itemClone = itemStack.clone();
                    if (backpackCanAdd < itemStack.getAmount()) {
                        itemClone.setAmount(backpackCanAdd);
                    }

                    ItemStack slotItem = similar.get().getSecond();

                    int currentAmount = slotItem.getAmount();
                    int canAdd = slotItem.getMaxStackSize() - currentAmount;

                    if (canAdd >= itemClone.getAmount()) {
                        slotItem.setAmount(slotItem.getAmount() + itemClone.getAmount());
                        added += itemClone.getAmount();
                        itemStack.setAmount(itemStack.getAmount() - itemClone.getAmount());
                        break;

                    } else {
                        int adding = itemClone.getAmount() - canAdd;
                        slotItem.setAmount(slotItem.getAmount() + adding);
                        if (itemClone.getAmount() == itemStack.getAmount())
                            itemStack.setAmount(adding);
                        else
                            itemStack.setAmount(itemStack.getAmount() - canAdd);
                        added += canAdd;
                    }

                } else {
                    Optional<OPair<Integer, ItemStack>> firstNull = data.firstNull();
                    if (!firstNull.isPresent()) {
                        if (!(config instanceof SimpleBackPackConfig)) break;
                        // Check how much we can still add

                        int canFit = getCapacity() - getUsed();
                        if (canFit <= 0) break;

                        int i = data.allocateMore();
                        if (canFit > itemStack.getAmount()) {
                            added += itemStack.getAmount();
                            data.setItem(i, itemStack.clone());
                            itemStack.setAmount(0);

                        } else {
                            int adding = itemStack.getAmount() - canFit;
                            ItemStack sloItem = itemStack.clone();
                            sloItem.setAmount(adding);
                            data.setItem(i, sloItem);

                            itemStack.setAmount(itemStack.getAmount() - adding);
                            added += adding;
                        }
                    } else {
                        added += itemStack.getAmount();
                        data.setItem(firstNull.get().getFirst(), itemStack.clone());
                        itemStack.setAmount(0);
                    }
                }
            }
            if (added != startingAmount)
                addedItems.put(itemStack, added);
        }
        return addedItems;
    }

    @Override
    public Map<ItemStack, Integer> remove(ItemStack... itemStacks) {
        if (getUsed() == 0)
            return Arrays.stream(itemStacks).collect(Collectors.toMap(item -> item, item -> 0));

        Map<ItemStack, Integer> removedItems = new HashMap<>();
        for (ItemStack itemStack : itemStacks) {
            int startingAmount = itemStack.getAmount();
            int removed = 0;

            while (itemStack.getAmount() > 0) {
                Optional<OPair<Integer, ItemStack>> similar = data.findSimilar(itemStack, false);
                if (!similar.isPresent()) break;

                ItemStack slotItem = similar.get().getSecond();
                if (slotItem.getAmount() == itemStack.getAmount()) {
                    removed += itemStack.getAmount();
                    itemStack.setAmount(0);
                    data.setItem(similar.get().getFirst(), null);

                } else if (slotItem.getAmount() > itemStack.getAmount()) {
                    int removing = slotItem.getAmount() - itemStack.getAmount();
                    removed = itemStack.getAmount();
                    slotItem.setAmount(removing);
                    itemStack.setAmount(0);
                    if (slotItem.getAmount() == 0)
                        data.setItem(similar.get().getFirst(), null);

                } else if (slotItem.getAmount() < itemStack.getAmount()) {
                    int canRemove = itemStack.getAmount() - slotItem.getAmount();
                    data.setItem(similar.get().getFirst(), null);

                    itemStack.setAmount(canRemove);
                }
            }
            if (startingAmount != removed)
                removedItems.put(itemStack, removed);
        }

        return removedItems;
    }

    @Override
    public void update() {
        // Update the inventory
        int first = owner.getInventory().first(itemStack);
        owner.getInventory().setItem(first, nbtItem.getItem());
        owner.updateInventory();

        // Update the menu
        if (currentView != null)
            currentView.refresh();
    }

    @Override
    public void upgrade(int level) {
        config = config.getByLevel(level);
        data.setLevel(level);

        if (config instanceof AdvancedBackPackConfig) {
            data.updateDataAdvanced(lastRows, ((AdvancedBackPackConfig) config).getRows(), lastPages, ((AdvancedBackPackConfig) config).getPages());
            lastPages = ((AdvancedBackPackConfig) config).getPages();
            lastRows = ((AdvancedBackPackConfig) config).getRows();

        } else
            data.updateData();

        save();

        if (currentView != null)
            currentView.onUpgrade();
    }

    @Override
    public boolean isModified() {
        return data.hashCode() != hashcode;
    }
}
