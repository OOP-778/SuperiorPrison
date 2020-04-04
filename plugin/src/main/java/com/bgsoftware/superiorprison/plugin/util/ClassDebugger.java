package com.bgsoftware.superiorprison.plugin.util;

import static com.oop.orangeengine.main.Engine.getEngine;

public class ClassDebugger {

    public static void debug(Object object) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        getEngine().getLogger().printDebug("<" + clazz.getSimpleName() + "> " + object.toString());
    }
}
