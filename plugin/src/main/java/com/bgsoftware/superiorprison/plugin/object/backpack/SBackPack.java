package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.BackPackConfig;
import com.bgsoftware.superiorprison.plugin.menu.backpack.BackPackViewMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.oop.datamodule.DataHelper;
import com.oop.datamodule.SerializedData;
import com.oop.orangeengine.item.ItemStackUtil;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.main.util.data.pair.OTriplePair;
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
    private BackPackConfig config;

    @Getter
    private BackPackData data;
    private NBTItem nbtItem;
    private ItemStack itemStack;

    @Getter
    private Player owner;

    private JsonObject oldData;

    @Setter @Getter
    private BackPackViewMenu currentView;

    private SBackPack() {}

    public static SBackPack of(BackPackConfig config, Player player) {
        SBackPack backPack = new SBackPack();
        backPack.config = config;
        backPack.oldData = new JsonObject();
        backPack.owner = player;
        backPack.itemStack = config.getItem().getItemStack().clone();
        backPack.data = new BackPackData(backPack);
        backPack.nbtItem = new NBTItem(backPack.itemStack);

        for (int i = 1; i < backPack.config.getPages() + 1; i++) {
            Map<Integer, ItemStack> itemStackMap = new HashMap<>();
            backPack.data.stored.put(i, itemStackMap);

            for (int slot = 0; slot < config.getRows() * 9; slot++)
                itemStackMap.put(slot, null);
        }

        backPack.updateNbt();
        backPack.save();
        return backPack;
    }

    @SneakyThrows
    public SBackPack(@Nonnull ItemStack itemStack, Player player) {
        this.owner = player;
        this.itemStack = itemStack;
        this.nbtItem = new NBTItem(itemStack);
        Preconditions.checkArgument(nbtItem.hasKey(NBT_KEY), "The given item is not an backpack");

        String serialized = nbtItem.getString(NBT_KEY);
        oldData = DataHelper.gson().fromJson(serialized, JsonObject.class);

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

        config = SuperiorPrisonPlugin.getInstance().getBackPackController().getConfig(data.configId).orElseThrow(() -> new IllegalStateException("Failed to find backPack by id " + data.configId + " level " + data.level)).getByLevel(data.level);
        data.updateInventoryData();
    }

    @Override
    public int getCapacity() {
        return config.getPages() * (config.getRows() * 9 * 64);
    }

    @Override
    public int getCurrentLevel() {
        return data.getLevel();
    }

    @Override
    public int getUsed() {
        return getStored()
                .stream()
                .filter(Objects::nonNull)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    @Override
    public List<ItemStack> getStored() {
        Map<Integer, Map<Integer, ItemStack>> stored = data.getStored();
        List<ItemStack> itemStacks = new ArrayList<>();
        stored.values().forEach(map -> itemStacks.addAll(map.values()));
        return itemStacks;
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
        updateNbt();
        SerializedData serializedData = new SerializedData();
        data.serialize(serializedData);

        if (SuperiorPrisonPlugin.getInstance().getBackPackController().isPlayerBound()) {
            oldData.remove(owner.getUniqueId().toString());
            oldData.add(owner.getUniqueId().toString(), serializedData.getJsonObject());

        } else {
            oldData.remove("global");
            oldData.add("global", serializedData.getJsonObject());
        }

        nbtItem.setString(NBT_KEY, DataHelper.gson().toJson(oldData));
    }

    @Override
    public Map<ItemStack, Integer> add(ItemStack... itemStacks) {
        OPair<Integer, Integer> firstEmpty = data.firstEmpty();
        if (firstEmpty == null && getCapacity() == getUsed())
            return Arrays.stream(itemStacks).collect(Collectors.toMap(item -> item, ItemStack::getAmount));

        for (ItemStack itemStack : itemStacks) {
            while (itemStack.getAmount() != 0) {
                OTriplePair<Integer, Integer, ItemStack> similar = data.findSimilar(itemStack, true);
                if (similar != null) {
                    ItemStack slotItem = similar.getThird();

                    int currentAmount = slotItem.getAmount();
                    int canAdd = slotItem.getMaxStackSize() - currentAmount;

                    if (canAdd >= itemStack.getAmount()) {
                        slotItem.setAmount(slotItem.getAmount() + itemStack.getAmount());
                        itemStack.setAmount(0);
                        break;

                    } else {
                        int adding = itemStack.getAmount() - canAdd;
                        slotItem.setAmount(slotItem.getAmount() + adding);
                        itemStack.setAmount(adding);
                    }
                } else {
                    firstEmpty = data.firstEmpty();
                    if (firstEmpty == null) break;

                    data.setItem(firstEmpty.getFirst(), firstEmpty.getSecond(), itemStack.clone());
                    itemStack.setAmount(0);
                }
            }
        }

        Map<ItemStack, Integer> notAdded = new HashMap<>();
        for (ItemStack stack : itemStacks) {
            if (stack.getAmount() == 0) continue;
            notAdded.put(stack, stack.getAmount());
        }

        return notAdded;
    }

    @Override
    public Map<ItemStack, Integer> remove(ItemStack... itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            while (itemStack.getAmount() != 0) {
                OTriplePair<Integer, Integer, ItemStack> similar = data.findSimilar(itemStack, false);
                if (similar == null) break;

                ItemStack slotItem = similar.getThird();
                if (slotItem.getAmount() == itemStack.getAmount()) {
                    itemStack.setAmount(0);
                    data.setItem(similar.getFirst(), similar.getSecond(), null);

                } else if (slotItem.getAmount() > itemStack.getAmount()) {
                    int removing = slotItem.getAmount() - itemStack.getAmount();
                    slotItem.setAmount(removing);
                    itemStack.setAmount(0);

                } else if (slotItem.getAmount() < itemStack.getAmount()) {
                    int canRemove = itemStack.getAmount() - slotItem.getAmount();
                    data.setItem(similar.getFirst(), similar.getSecond(), null);

                    itemStack.setAmount(canRemove);
                }
            }
        }

        Map<ItemStack, Integer> notRemoved = new HashMap<>();
        for (ItemStack stack : itemStacks) {
            if (stack.getAmount() == 0) continue;
            notRemoved.put(stack, stack.getAmount());
        }

        return notRemoved;
    }

    @Override
    public void update() {
        // Update the inventory
        int first = owner.getInventory().first(itemStack);
        owner.getInventory().setItem(first, nbtItem.getItem());

        // Update the menu
        if (currentView != null)
            currentView.refresh();
    }

    @Override
    public void upgrade(int level) {
        config = config.getByLevel(level);
        data.level = level;
        data.updateInventoryData();
        save();

        if (currentView != null)
            currentView.onUpgrade();
    }
}
