package com.bgsoftware.superiorprison.plugin.object.mine.effects;

import com.bgsoftware.superiorprison.api.data.mine.effects.MineEffect;
import com.bgsoftware.superiorprison.api.data.mine.effects.MineEffects;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import lombok.Getter;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SMineEffects implements MineEffects, SerializableObject, Attachable<SNormalMine> {

    @Getter
    private SNormalMine mine;

    private final Map<PotionEffectType, SMineEffect> effects = new ConcurrentHashMap<>();

    @Override
    public Optional<MineEffect> get(PotionEffectType type) {
        return Optional.ofNullable(effects.get(type));
    }

    @Override
    public void remove(MineEffect effect) {
        effects.remove(effect.getType());
    }

    @Override
    public void remove(PotionEffectType type) {
        effects.remove(type);
    }

    public void add(SMineEffect effect) {
        effects.put(effect.getType(), effect);
    }

    @Override
    public SMineEffect add(PotionEffectType type, int amplifier) {
        SMineEffect effect = effects.get(type);
        if (effect == null) {
            effect = new SMineEffect(type, amplifier);
            effects.put(type, effect);

        } else
            effect.setAmplifier(amplifier);

        return effect;
    }

    @Override
    public boolean has(PotionEffectType type) {
        return effects.containsKey(type);
    }

    @Override
    public void serialize(SerializedData serializedData) {
        JsonArray array = new JsonArray();
        for (SMineEffect effect : effects.values()) {
            JsonObject object = new JsonObject();
            object.addProperty("type", effect.getType().getName());
            object.addProperty("amplifier", effect.getAmplifier());
            array.add(object);
        }
        serializedData.getJsonObject().add("effects", array);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        serializedData
                .getElement("effects")
                .map(JsonElement::getAsJsonArray)
                .ifPresent(array -> {
                    for (JsonElement jsonElement : array) {
                        JsonObject object = jsonElement.getAsJsonObject();
                        SMineEffect effect = new SMineEffect(PotionEffectType.getByName(object.getAsJsonPrimitive("type").getAsString()), object.getAsJsonPrimitive("amplifier").getAsInt());
                        effects.put(effect.getType(), effect);
                    }
                });
    }

    @Override
    public void attach(SNormalMine obj) {
        this.mine = obj;
    }

    @Override
    public Set<MineEffect> get() {
        return effects.values().stream().map(effect -> (MineEffect) effect).collect(Collectors.toSet());
    }

    @Override
    public void clear() {
        effects.clear();
    }

    public void addAll(Collection<MineEffect> effects) {
        for (MineEffect effect : effects) {
            this.effects.put(effect.getType(), (SMineEffect) effect);
        }
    }
}
