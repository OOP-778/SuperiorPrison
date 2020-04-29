package com.bgsoftware.superiorprison.plugin.object.mine.effects;

import com.bgsoftware.superiorprison.api.data.mine.effects.MineEffect;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Getter
@Setter
@AllArgsConstructor
public class SMineEffect implements MineEffect, SerializableObject {

    private PotionEffectType type;
    private int amplifier;

    public SMineEffect() {}

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("type", type.getName());
        serializedData.write("amplifier", amplifier);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        type = PotionEffectType.getByName(serializedData.applyAs("type", String.class));
        amplifier = serializedData.applyAs("amplifier", int.class);
    }

    public PotionEffect create() {
        return new PotionEffect(type, Integer.MAX_VALUE, amplifier, false, false);
    }
}
