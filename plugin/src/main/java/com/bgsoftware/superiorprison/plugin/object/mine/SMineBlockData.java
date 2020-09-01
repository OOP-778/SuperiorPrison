package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.MineBlockData;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.bgsoftware.superiorprison.plugin.util.ClassDebugger;
import com.oop.datamodule.gson.JsonArray;
import com.oop.datamodule.gson.JsonObject;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import lombok.Getter;
import org.bukkit.Material;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SMineBlockData implements Attachable<SMineGenerator>, MineBlockData, SerializableObject {

    private SMineGenerator generator;

    // Material, current value, starting value
    private final Map<OMaterial, OPair<Long, Long>> materials = new ConcurrentHashMap<>();

    @Getter
    private long blocksLeft;

    public void reset() {
        materials.values().forEach(pair -> pair.setFirst(pair.getSecond()));
        blocksLeft = generator.getBlocksInRegion();
    }

    public void initialize() {
        materials.clear();
        for (OMaterial cachedMaterial : generator.getCachedMaterials()) {
            if (cachedMaterial == null) continue;
            materials.merge(cachedMaterial, new OPair<>(1L, 1L), (f, s) -> {
                OPair<Long, Long> newPair = new OPair<>(0L, 0L);

                newPair.setFirst(f.getFirst() + s.getFirst());
                newPair.setSecond(f.getSecond() + s.getSecond());
                return newPair;
            });
        }

        blocksLeft = generator.getBlocksInRegion();
    }

    public boolean isEmpty() {
        return blocksLeft == 0;
    }

    public void decrease(OMaterial material, long amount) {
        materials.merge(material, new OPair<>(amount, 0L), (f, s) -> f.setFirst(Math.max(f.getFirst() - s.getFirst(), 0L)));
        blocksLeft = Math.max(blocksLeft - amount, 0L);
    }

    @Override
    public void attach(SMineGenerator obj) {
        this.generator = obj;
    }

    @Override
    public long getMaterialLeft(Material material) {
        return getMaterialLeft(OMaterial.matchMaterial(material));
    }

    @Override
    public void decrease(Material material, long amount) {
        decrease(OMaterial.matchMaterial(material), amount);
    }

    public long getMaterialLeft(OMaterial material) {
        return Optional.ofNullable(materials.get(material)).map(OPair::getFirst).orElse(0L);
    }

    @Override
    public int getPercentageLeft() {
        return (int) (blocksLeft * 100.0 / generator.getBlocksInRegion());
    }

    @Override
    public void serialize(SerializedData serializedData) {
        JsonArray array = new JsonArray();
        materials.forEach((key, value) -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("material", key.name());
            jsonObject.addProperty("left", value.getFirst());
            jsonObject.addProperty("was", value.getSecond());
            array.add(jsonObject);
        });
        serializedData.write("data", array);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        serializedData
                .applyAsCollection("data")
                .forEach(element -> {
                    JsonObject object = element.getJsonElement().getAsJsonObject();
                    OMaterial material = OMaterial.valueOf(object.get("material").getAsString());
                    long left = object.get("left").getAsLong();
                    long was = object.get("was").getAsLong();
                    materials.put(material, new OPair<>(left, was));
                });
        blocksLeft = materials.values()
                .stream()
                .mapToLong(OPair::getFirst)
                .sum();
    }
}
