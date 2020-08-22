package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.util.DataUtil;
import com.oop.orangeengine.item.ItemStackUtil;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.main.util.data.pair.OTriplePair;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.oop.datamodule.util.DataUtil.wrap;

@Getter
public class BackPackData implements SerializableObject {

    // Page, Slot, Item
    public Map<Integer, Map<Integer, ItemStack>> stored = new HashMap<>();

    public int level;
    public @NonNull String configId;
    public @NonNull Player owner;
    private @NonNull SBackPack holder;

    @Setter
    private boolean sell = false;

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

        // Is sell
        serializedData.write("sell", sell);

        // Items
        JsonArray pagesArray = new JsonArray();
        stored.forEach((page, itemData) -> {
            JsonObject itemsObject = new JsonObject();
            itemData.forEach((slot, item) -> itemsObject.add(slot + "", wrap(item)));
            pagesArray.add(itemsObject);
        });

        serializedData.getJsonElement().getAsJsonObject().add("items", pagesArray);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        this.level = serializedData.applyAs("level", int.class);
        this.configId = serializedData.applyAs("configId", String.class);
        this.sell = serializedData.applyAs("sell", boolean.class, () -> false);

        JsonArray itemsArray = serializedData.getJsonElement().getAsJsonObject().getAsJsonArray("items");
        int page = 1;
        for (JsonElement element : itemsArray) {
            JsonObject itemsData = element.getAsJsonObject();

            Map<Integer, ItemStack> pageData = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : itemsData.entrySet()) {
                pageData.put(Integer.parseInt(entry.getKey()), DataUtil.fromElement(entry.getValue(), ItemStack.class));
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

                if (ItemStackUtil.isSimilar(item, to) && (!amountCheck || item.getAmount() != item.getMaxStackSize()))
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackPackData that = (BackPackData) o;
        return level == that.level &&
                stored.equals(that.stored);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stored, level, sell);
    }
}
