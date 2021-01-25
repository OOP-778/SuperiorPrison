package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.main.StorageSection;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.data.SStatisticHolder;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.lib.google.gson.JsonElement;
import com.oop.datamodule.lib.google.gson.JsonParseException;
import com.oop.datamodule.lib.google.gson.JsonPrimitive;
import com.oop.datamodule.lib.google.gson.TypeAdapter;
import com.oop.datamodule.lib.google.gson.stream.JsonReader;
import com.oop.datamodule.lib.google.gson.stream.JsonToken;
import com.oop.datamodule.lib.google.gson.stream.JsonWriter;
import com.oop.datamodule.universal.UniversalStorage;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import com.oop.orangeengine.nbt.NBTContainer;
import com.oop.orangeengine.nbt.NBTItem;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                        String s = reader.nextString();
                        return deserialize(StringUtils.replaceChars(s, "\\", ""));
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
                getStorages().forEach(storage -> storage.save(true));

                getPrisonerHolder().cleanInvalids();
                getPrisonerHolder().initializeCache();
            }
        });

//        SuperiorPrisonPlugin.getInstance().getPluginComponentController()
//                .listenForReload(ConfigController.class, configController -> {
//                    StorageSection storageSection = SuperiorPrisonPlugin.getInstance().getMainConfig().getStorageSection();
//
//                    save(false, () -> {
//                        for (Storage<?> storage : getStorages()) {
//                            Storage currentImplementation = ((UniversalStorage) storage).getCurrentImplementation();
//                            Storage storageNew = storageSection.getStorageProvider().apply((UniversalStorage) storage);
//
//                            if (currentImplementation.getClass().equals(storageNew.getClass())) continue;
//
//                            ((UniversalStorage) storage).currentImplementation(storageNew);
//                            storage.save(true);
//                        }
//                    });
//                });
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
