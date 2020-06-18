package com.bgsoftware.superiorprison.api.data.mine.effects;

import org.bukkit.potion.PotionEffectType;

public interface MineEffect {
    // Get effect type
    PotionEffectType getType();

    // Get amplifier
    int getAmplifier();

    // Set amplifier
    void setAmplifier(int amplifier);
}
