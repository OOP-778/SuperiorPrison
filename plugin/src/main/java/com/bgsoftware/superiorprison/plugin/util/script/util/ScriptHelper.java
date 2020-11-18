package com.bgsoftware.superiorprison.plugin.util.script.util;

import com.bgsoftware.superiorprison.plugin.util.script.ScriptEngine;
import com.bgsoftware.superiorprison.plugin.util.script.function.Function;
import com.bgsoftware.superiorprison.plugin.util.script.util.function.FunctionValidator;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class ScriptHelper {
    private static Map<String, Object> mapOfArray(Object... objects) {
        if (objects.length % 2 != 0)
            throw new IllegalStateException("Failed to convert objects to map, because the size is not even!");

        Map<String, Object> map = new HashMap<>();

        int len = objects.length;
        int i = 0;

        boolean inside = true;
        while (inside) {
            Object key = Objects.requireNonNull(objects[i++], "Key cannot be null!");
            Object value = Objects.requireNonNull(objects[i++], "Value cannot be null!");
            map.put(
                    key.toString(),
                    value
            );
            if (i == len)
                inside = false;
        }

        return map;
    }

    public static <T> T catcher(String errorMessage, Supplier<T> consumer) {
        try {
            return consumer.get();
        } catch (Throwable throwable) {
            throw new IllegalStateException(errorMessage, throwable);
        }
    }

    public static FunctionValidator tryInitFunction(String in, GlobalVariableMap variableMap) {
        try {
            String cloneIn = variableMap.initializeVariables(in, null);
            return new FunctionValidator(ScriptEngine.getInstance().initializeFunction(cloneIn, variableMap), in);
        } catch (Throwable throwable) {
            System.out.println("Variable Map bin from last error: " + PasteHelper.paste(variableMap));
            throw new IllegalStateException("Failed to initialize function by: `" + in + "`", throwable);
        }
    }

    public static <T> Function<T> getFunctionFromObject(T object) {
        return new Function<T>() {
            @Override
            public void initialize(String string, GlobalVariableMap variableMap) {}

            @Override
            public Class<T> getType() {
                return (Class<T>) object.getClass();
            }

            @Override
            public boolean isCacheable() {
                return true;
            }

            @Override
            public T execute(GlobalVariableMap globalVariables) {
                return object;
            }

            @Override
            public String getId() {
                return "function from object";
            }
        };
    }
}
