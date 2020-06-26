package com.bgsoftware.superiorprison.plugin.util;

import static com.oop.orangeengine.main.Engine.getEngine;

public class ClassDebugger {

    public static void debug(Object object) {
        Class<?> clazz = null;
        String methodName = null;
        try {
            StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
            clazz = Class.forName(ste.getClassName());
            methodName = ste.getMethodName();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            getEngine().getLogger().printDebug("<{}:{}>: {}", clazz.getSimpleName(), methodName, object);
        } catch (InternalError error) {
            getEngine().getLogger().printDebug("<{}>: {}", "Malformed", object);
        }
    }
}
