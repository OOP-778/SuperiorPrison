package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.BackPackConfig;
import com.bgsoftware.superiorprison.plugin.config.backpack.SimpleBackPackConfig;
import com.google.common.base.Preconditions;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.gson.JsonArray;
import com.oop.datamodule.gson.JsonElement;
import com.oop.datamodule.gson.JsonObject;
import com.oop.datamodule.util.DataUtil;
import com.oop.orangeengine.item.ItemStackUtil;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Predicate;

import static com.oop.datamodule.util.DataUtil.wrap;

@Getter
public class BackPackData implements SerializableObject {

    private UUID uuid;

    @Setter
    private int level;
    private @NonNull String configId;
    private @NonNull
    final SBackPack holder;

    @Setter
    private boolean sell = false;

    private ItemStack[] stored = new ItemStack[0];

    public BackPackData(SBackPack backPack) {
        this.holder = backPack;
        if (holder.getConfig() != null) {
            this.level = holder.getConfig().getLevel();
            this.configId = holder.getConfig().getId();
        }
    }

    public Optional<OPair<Integer, ItemStack>> first(Predicate<ItemStack> filter) {
        for (int i = 0; i < stored.length; i++) {
            ItemStack itemStack = stored[i];
            if (filter.test(itemStack))
                return Optional.of(new OPair<>(i, itemStack));
        }

        return Optional.empty();
    }

    public Optional<OPair<Integer, ItemStack>> firstNonNull() {
        return first(Objects::nonNull);
    }

    public Optional<OPair<Integer, ItemStack>> firstNull() {
        return first(Objects::isNull);
    }

    public void setItem(int index, ItemStack itemStack) {
        Preconditions.checkArgument(!(index >= stored.length), "index is too big! (" + index + "/" + stored.length + ")");
        stored[index] = itemStack;
    }

    public void updateData() {
        if (stored.length == holder.getSlots()) return;
    }

    public void updateDataAdvanced(int oldRows, int newRows, int oldPages, int newPages) {
        if (stored.length == 0) {
            stored = new ItemStack[newRows * newPages * 9];
            return;
        }

        if (oldRows == newRows && newPages == oldPages) return;

        ItemStack[][] pagedData = new ItemStack[oldPages][oldRows * 9];
        ItemStack[] oldData = stored;

        stored = new ItemStack[newRows * 9 * newPages];

        // Old Data Splitting begin
        int slot = 0;
        int page = 0;
        for (int i = 0; i < oldData.length; i++) {
            // If it reaches the page limit, increase the page and reset slot
            if (slot == oldRows * 9) {
                page++;
                slot = 0;
            }

            ItemStack oldDatum = oldData[i];
            if (oldDatum != null)
                pagedData[page][slot] = oldDatum;

            slot++;
        }
        // Old data splitting end

        // Data Migration begin
        slot = 0;
        page = 0;
        for (int i = 0; i < stored.length; i++) {
            // If it reaches the page limit, increase the page and reset slot
            if (slot == oldRows * 9) {
                page++;
                slot = 0;
            }

            if (pagedData.length > page && pagedData[page].length > slot)
                stored[i] = pagedData[page][slot];

            slot++;
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

        // Write UUID
        serializedData.write("uuid", uuid);

        // Items
        JsonArray dataArray = new JsonArray();
        for (int i = 0; i < stored.length; i++) {
            ItemStack item = stored[i];
            if (item != null && item.getType() != Material.AIR) {
                JsonArray itemArray = new JsonArray();
                itemArray.add(i);
                itemArray.add(wrap(item));
                dataArray.add(itemArray);
            }
        }

        serializedData.getJsonElement().getAsJsonObject().add("items", dataArray);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        this.level = serializedData.applyAs("level", int.class);
        this.configId = serializedData.applyAs("configId", String.class);
        this.sell = serializedData.applyAs("sell", boolean.class, () -> false);
        this.uuid = serializedData.applyAs("uuid", UUID.class, UUID::randomUUID);

        JsonArray itemsArray = serializedData.getJsonElement().getAsJsonObject().getAsJsonArray("items");
        if (itemsArray.size() == 0) return;

        boolean isNewMethod = !itemsArray.get(0).isJsonObject();
        if (!isNewMethod) {
            int page = 1;
            Map<Integer, ItemStack[]> halfConvertedData = new HashMap<>();
            int pageSize = 0;

            // Converting data into easier use
            for (JsonElement element : itemsArray) {
                JsonObject itemsData = element.getAsJsonObject();

                if (pageSize == 0)
                    pageSize = itemsData.entrySet().size();

                ItemStack[] arrayPageData = new ItemStack[pageSize];
                for (int i = 0; i < arrayPageData.length; i++)
                    arrayPageData[i] = DataUtil.fromElement(itemsData.get("" + i++), ItemStack.class);

                halfConvertedData.put(page, arrayPageData);
                page++;
            }

            // Migrating data
            stored = new ItemStack[pageSize * halfConvertedData.size()];
            page = 1;
            int slot = 0;
            for (int i = 0; i < stored.length; i++) {
                if (slot == pageSize) {
                    slot = 0;
                    page++;
                }

                stored[i] = halfConvertedData.get(page)[slot];
                slot++;
            }
        } else {
            BackPackConfig<?> config = SuperiorPrisonPlugin.getInstance().getBackPackController().getConfig(configId).orElseThrow(() -> new IllegalStateException("Failed to find backPack by id " + configId + " level " + level)).getByLevel(level);
            if (config instanceof SimpleBackPackConfig) {
                List<ItemStack> list = new ArrayList<>();
                for (JsonElement element : itemsArray) {
                    JsonArray itemArray = element.getAsJsonArray();
                    ItemStack itemStack = DataUtil.fromElement(itemArray.get(1), ItemStack.class);
                    list.add(itemStack);
                }

                stored = list.toArray(new ItemStack[0]);
            } else {
                stored = new ItemStack[config.getCapacity() / 64];
                for (JsonElement element : itemsArray) {
                    JsonArray itemArray = element.getAsJsonArray();
                    int index = itemArray.get(0).getAsInt();
                    ItemStack itemStack = DataUtil.fromElement(itemArray.get(1), ItemStack.class);
                    stored[index] = itemStack;
                }
            }
        }
    }

    public int allocateMore() {
        stored = Arrays.copyOfRange(stored, 0, stored.length + 1);
        return stored.length - 1;
    }

    public Optional<OPair<Integer, ItemStack>> findSimilar(ItemStack to, boolean amountCheck) {
        for (int i = 0; i < stored.length; i++) {
            ItemStack item = stored[i];
            if (item != null) {
                if (ItemStackUtil.isSimilar(item, to) && (!amountCheck || item.getAmount() != item.getMaxStackSize()))
                    return Optional.of(new OPair<>(i, item));
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackPackData that = (BackPackData) o;
        return level == that.level &&
                sell == that.sell &&
                Objects.equals(configId, that.configId) &&
                Arrays.equals(stored, that.stored);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(level, configId, sell);
        result = 31 * result + Arrays.hashCode(stored);
        return result;
    }
}
