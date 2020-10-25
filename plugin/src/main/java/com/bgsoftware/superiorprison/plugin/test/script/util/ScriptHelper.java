package com.bgsoftware.superiorprison.plugin.test.script.util;

import com.bgsoftware.superiorprison.plugin.test.script.ScriptEngine;
import com.bgsoftware.superiorprison.plugin.test.script.util.function.FunctionValidator;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;

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
            throw new IllegalStateException("Failed to initialize function by: `" + in + "`", throwable);
        }
    }
}
