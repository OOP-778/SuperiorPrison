package com.bgsoftware.superiorprison.plugin.object.mine.effects;

import com.bgsoftware.superiorprison.api.data.mine.effects.MineEffect;
import com.bgsoftware.superiorprison.api.data.mine.effects.MineEffects;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.util.DataUtil;
import lombok.Getter;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SMineEffects implements MineEffects, SerializableObject, Attachable<SNormalMine> {

    @Getter
    private SNormalMine mine;

    private Map<PotionEffectType, SMineEffect> effects = new ConcurrentHashMap<>();

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
        serializedData.write("effects", effects);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        serializedData
                .applyAsMap("effects")
                .forEach(pair -> {
                    PotionEffectType type = DataUtil.fromElement(pair.getKey(), PotionEffectType.class);
                    SMineEffect effect = DataUtil.fromElement(pair.getValue(), SMineEffect.class);
                    effects.put(type, effect);
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
}
