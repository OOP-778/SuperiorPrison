package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.util.OSimpleReflection;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TPS {
    private static final Method
            getServerMethod;

    private static final Field
            recentTpsField;

    static {
        Class<?> MINECRAFT_SERVER_CLASS = OSimpleReflection.findClass("{nms}.MinecraftServer");
        getServerMethod = OSimpleReflection.getMethod(MINECRAFT_SERVER_CLASS, "getServer");
        recentTpsField = OSimpleReflection.getField(MINECRAFT_SERVER_CLASS, "recentTps");
    }

    @SneakyThrows
    public static double getCurrentTps() {
        double[] array = (double[]) recentTpsField.get(getServerMethod.invoke(null));
        return Math.min(20, array[0]);
    }
}
