package com.bgsoftware.superiorprison.api.data.mine.effects;

import org.bukkit.potion.PotionEffectType;

import java.util.Optional;
import java.util.Set;

public interface MineEffects {

    // Get specific potion effect if available
    Optional<MineEffect> get(PotionEffectType type);

    // Remove mine effect
    void remove(MineEffect effect);

    // Remove potion effect
    void remove(PotionEffectType type);

    // Add potion effect
    MineEffect add(PotionEffectType type, int amplifier);

    // Check if has an potion effect
    boolean has(PotionEffectType type);

    // Get set of effects
    Set<MineEffect> get();

    // Clear all the effects
    void clear();

    // Reapply all effects
    void reapplyEffects();

    // Clear all the effects from the prisoner
    void clearEffects();
}