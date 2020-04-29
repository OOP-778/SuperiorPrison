package com.bgsoftware.superiorprison.api.data.mine.effects;

import org.bukkit.potion.PotionEffectType;

public interface MineEffect {
    PotionEffectType getType();
    int getAmplifier();

    void setAmplifier(int amplifier);
}
