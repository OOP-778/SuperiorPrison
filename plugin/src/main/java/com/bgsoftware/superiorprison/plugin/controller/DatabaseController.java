package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.data.SStatisticHolder;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.lib.google.gson.*;
import com.oop.datamodule.lib.google.gson.stream.JsonReader;
import com.oop.datamodule.lib.google.gson.stream.JsonToken;
import com.oop.datamodule.lib.google.gson.stream.JsonWriter;
import com.oop.orangeengine.nbt.NBTContainer;
import com.oop.orangeengine.nbt.NBTItem;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.oop.orangeengine.main.Engine.getEngine;

@Getter
public class DatabaseController extends StorageRegistry {
    private final String UNICODE_REGEX = "\\\\u([0-9a-f]{4})";
    private final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9a-f]{4})");

    private final SPrisonerHolder prisonerHolder;
    private final SMineHolder mineHolder;
    private final SStatisticHolder statisticHolder;

    public DatabaseController() {
        StorageInitializer.getInstance().registerAdapter(ItemStack.class, true, new TypeAdapter<ItemStack>() {
            @Override
            public void write(JsonWriter writer, ItemStack itemStack) throws IOException {
                if (itemStack == null || itemStack.getType() == Material.AIR)
                    writer.nullValue();
                else
                    writer.value(serialize(itemStack).getAsString());
            }

            @Override
            public ItemStack read(JsonReader reader) throws IOException {
                JsonToken peek = reader.peek();
                switch (peek) {
                    case STRING:
                        return deserialize(reader.nextString());
                    case NULL:
                        return null;
                }

                return null;
            }
        });

        this.prisonerHolder = new SPrisonerHolder(this);
        this.mineHolder = new SMineHolder(this);
        this.statisticHolder = new SStatisticHolder(this);

        AtomicInteger integer = new AtomicInteger();
        load(false, () -> {
            integer.incrementAndGet();
            if (integer.get() == getStorages().size()) {
                getEngine().getLogger().print("Loaded {} mines", getMineHolder().getMines().size());
                getEngine().getLogger().print("Loaded {} prisoners", getPrisonerHolder().getPrisonerMap().size());
                getStorages().forEach(storage -> storage.save(true));

                getPrisonerHolder().cleanInvalids();
                getPrisonerHolder().initializeCache();
            }
        });
    }

    public ItemStack deserialize(String serializedItem) throws JsonParseException {
        return NBTItem.convertNBTtoItem(new NBTContainer(utf8(serializedItem)));
    }

    public JsonElement serialize(ItemStack itemStack) {
        return new JsonPrimitive(NBTItem.convertItemtoNBT(itemStack).asNBTString());
    }

    public String utf8(String text) {
        Matcher matcher = UNICODE_PATTERN.matcher(text);
        StringBuffer decodedMessage = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(decodedMessage, String.valueOf((char) Integer.parseInt(matcher.group(1), 16)));
        }

        matcher.appendTail(decodedMessage);
        return decodedMessage.toString();
    }
}
