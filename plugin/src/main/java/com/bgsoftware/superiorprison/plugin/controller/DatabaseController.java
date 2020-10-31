package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.config.main.MainConfig;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.data.SStatisticHolder;
import com.oop.datamodule.StorageHolder;
import com.oop.datamodule.StorageInitializer;
import com.oop.datamodule.database.DatabaseWrapper;
import com.oop.datamodule.gson.*;
import com.oop.datamodule.gson.stream.JsonReader;
import com.oop.datamodule.gson.stream.JsonToken;
import com.oop.datamodule.gson.stream.JsonWriter;
import com.oop.orangeengine.main.task.StaticTask;
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
public class DatabaseController extends StorageHolder {

    private final String UNICODE_REGEX = "\\\\u([0-9a-f]{4})";
    private final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9a-f]{4})");

    private final SPrisonerHolder prisonerHolder;
    private final SMineHolder mineHolder;
    private final SStatisticHolder statisticHolder;

    private final DatabaseWrapper database;

    public DatabaseController(MainConfig config) {
        database = config.getDatabase().getDatabase();

        StorageInitializer.getInstance().registerAdapter(ItemStack.class, true, new TypeAdapter<ItemStack>() {
            @Override
            public void write(JsonWriter writer, ItemStack itemStack) throws IOException {
                if (itemStack == null)
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
        registerStorage(prisonerHolder);
        registerStorage(mineHolder);
        registerStorage(statisticHolder);

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
        return (itemStack == null || itemStack.getType() == Material.AIR) ? JsonNull.INSTANCE : new JsonPrimitive(NBTItem.convertItemtoNBT(itemStack).asNBTString());
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
