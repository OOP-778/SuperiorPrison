package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.AdvancedBackPackConfig;
import com.bgsoftware.superiorprison.plugin.config.backpack.BackPackConfig;
import com.bgsoftware.superiorprison.plugin.menu.backpack.BackPackViewMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.google.common.base.Preconditions;
import com.oop.datamodule.gson.JsonObject;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.StorageInitializer;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.nbt.NBTItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
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
    private NewBackPackData data;
    private NBTItem nbtItem;
    private ItemStack itemStack;

    @Getter
    private Player owner;

    private JsonObject oldData;

    @Setter
    @Getter
    private BackPackViewMenu currentView;
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
        backPack.data = new NewBackPackData(backPack);
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

        data = new NewBackPackData(this);
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
        return getCapacity() / 64;
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
        this.nbtItem = new NBTItem(new OMenuButton.ButtonItemBuilder(config.getItem().makeUnstackable())
                .getItemStackWithPlaceholdersMulti(this));
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
                    ItemStack slotItem = similar.get().getSecond();

                    int currentAmount = slotItem.getAmount();
                    int canAdd = slotItem.getMaxStackSize() - currentAmount;

                    if (canAdd >= itemStack.getAmount()) {
                        slotItem.setAmount(slotItem.getAmount() + itemStack.getAmount());
                        added += itemStack.getAmount();
                        itemStack.setAmount(0);
                        break;

                    } else {
                        int adding = itemStack.getAmount() - canAdd;
                        slotItem.setAmount(slotItem.getAmount() + adding);
                        itemStack.setAmount(adding);
                        added += canAdd;
                    }
                } else {
                    Optional<OPair<Integer, ItemStack>> firstNonNull1 = data.firstNonNull();
                    if (!firstNonNull.isPresent()) break;

                    added += itemStack.getAmount();
                    data.setItem(firstNonNull.get().getFirst(), itemStack.clone());
                    itemStack.setAmount(0);
                }
            }
            if (added != startingAmount)
                addedItems.put(itemStack, added);
        }
        return addedItems;
    }

    @Override
    public Map<ItemStack, Integer> remove(ItemStack... itemStacks) {
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
