package com.bgsoftware.superiorprison.plugin.util.input;

import lombok.NonNull;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class Input {
    public static PlayerInput<Integer> integerInput(@NonNull HumanEntity player) {
        return new PlayerInput<Integer>(((Player) player))
                .parser(string -> {
                    try {
                        return Integer.parseInt(string);
                    } catch (Throwable throwable) {
                        throw new IllegalStateException("Invalid number: " + string);
                    }
                });
    }

    public static PlayerInput<Double> doubleInput(@NonNull HumanEntity player) {
        return new PlayerInput<Double>(((Player) player))
                .parser(string -> {
                    try {
                        return Double.parseDouble(string);
                    } catch (Throwable throwable) {
                        throw new IllegalStateException("Invalid number: " + string);
                    }
                });
    }

    public static PlayerInput<BigDecimal> bigDecimalInput(@NonNull HumanEntity player) {
        return new PlayerInput<BigDecimal>(((Player) player))
                .parser(string -> {
                    try {
                        return new BigDecimal(string);
                    } catch (Throwable throwable) {
                        throw new IllegalStateException("Invalid number: " + string);
                    }
                });
    }
}
