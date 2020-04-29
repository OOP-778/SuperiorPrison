package com.bgsoftware.superiorprison.api.data.mine.effects;

import org.bukkit.potion.PotionEffectType;

import java.util.Optional;
import java.util.Set;

public interface MineEffects {

    Optional<MineEffect> get(PotionEffectType type);

    void remove(MineEffect effect);

    void remove(PotionEffectType type);

    MineEffect add(PotionEffectType type, int amplifier);

    boolean has(PotionEffectType type);

    Set<MineEffect> get();
}