package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.oop.datamodule.DataHelper;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import com.oop.orangeengine.item.ItemStackUtil;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.main.util.data.pair.OTriplePair;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class BackPackData implements SerializableObject {

    // Page, Slot, Item
    public Map<Integer, Map<Integer, ItemStack>> stored = new HashMap<>();

    public int level;
    public @NonNull String configId;
    public @NonNull Player owner;
    private @NonNull SBackPack holder;

    public BackPackData(SBackPack holder) {
        this.holder = holder;

        if (holder.getConfig() != null) {
            this.level = holder.getConfig().getLevel();
            this.configId = holder.getConfig().getId();
        }
    }

    @Override
    public void serialize(SerializedData serializedData) {
        // Level
        serializedData.write("level", level);

        // Config id
        serializedData.write("configId", configId);

        // Items
        JsonArray pagesArray = new JsonArray();
        stored.forEach((page, itemData) -> {
            JsonObject itemsObject = new JsonObject();
            itemData.forEach((slot, item) -> itemsObject.add(slot + "", DataHelper.ITEMSTACK_TO_ELEMENT.apply(item)));
            pagesArray.add(itemsObject);
        });

        serializedData.getJsonObject().add("items", pagesArray);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        this.level = serializedData.applyAs("level", int.class);
        this.configId = serializedData.applyAs("configId", String.class);

        JsonArray itemsArray = serializedData.getJsonObject().getAsJsonArray("items");
        int page = 1;
        for (JsonElement element : itemsArray) {
            JsonObject itemsData = element.getAsJsonObject();

            Map<Integer, ItemStack> pageData = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : itemsData.entrySet()) {
                pageData.put(Integer.parseInt(entry.getKey()), DataHelper.ELEMENT_TO_ITEMSTACK.apply(entry.getValue()));
            }

            stored.put(page, pageData);
            page++;
        }
    }

    public OTriplePair<Integer, Integer, ItemStack> findSimilar(ItemStack to, boolean amountCheck) {
        AtomicReference<OTriplePair<Integer, Integer, ItemStack>> ref = new AtomicReference<>(null);
        stored.forEach((page, slotData) -> {
            if (ref.get() != null) return;

            slotData.forEach((slot, item) -> {
                if (ref.get() != null) return;
                if (item == null) return;

                if (ItemStackUtil.isSimilar(item, to) && (amountCheck && item.getAmount() != item.getMaxStackSize()))
                    ref.set(new OTriplePair<>(page, slot, item));
            });
        });
        return ref.get();
    }

    public OPair<Integer, Integer> firstEmpty() {
        AtomicReference<OPair<Integer, Integer>> ref = new AtomicReference<>(null);
        stored.forEach((page, slotData) -> {
            if (ref.get() != null) return;
            slotData.forEach((slot, item) -> {
                if (ref.get() != null) return;
                if (item == null) {
                    ref.set(new OPair<>(page, slot));
                }
            });
        });
        return ref.get();
    }

    public void setItem(int page, int slot, ItemStack itemStack) {
        Map<Integer, ItemStack> slotData = Objects.requireNonNull(stored.get(page), "Page given is too big! " + page + "/ " + stored.size());
        slotData.remove(slot);

        if (itemStack == null || itemStack.getAmount() == 0)
            itemStack = null;

        slotData.put(slot, itemStack);
    }

    public void updateInventoryData() {
        for (int i = 1; i < holder.getConfig().getPages() + 1; i++) {
            Map<Integer, ItemStack> pageData = stored.computeIfAbsent(i, page -> new HashMap<>());

            for (int slot = 0; slot < holder.getConfig().getRows() * 9; slot++) {
                if (pageData.containsKey(slot)) continue;

                pageData.put(slot, null);
            }
        }
    }
}
