package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.oop.datamodule.DataHelper;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BackPackData implements SerializableObject {

    // Page, Slot, Item
    public Map<Integer, Map<Integer, ItemStack>> stored = new HashMap<>();

    public int level;

    public @NonNull String configId;

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
}
