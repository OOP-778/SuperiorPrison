package com.bgsoftware.superiorprison.plugin.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.oop.orangeengine.main.Engine.getEngine;

public class ClassDebugger {

    public static void debug(Object text, Object... placeholders) {
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
            getEngine().getLogger().printDebug("<{}:{}>: " + text, mergeArrays(new Object[]{clazz, methodName}, placeholders));
        } catch (InternalError error) {
            getEngine().getLogger().printDebug("<{}>: " + text, mergeArrays(new Object[]{"Malformed"}, placeholders));
        }
    }

    private static Object[] mergeArrays(Object[]... arr) {
        List<Object> allObjects = new LinkedList<>();
        for (Object[] objects : arr) {
            allObjects.addAll(Arrays.asList(objects));
        }

        return allObjects.toArray();
    }
}
