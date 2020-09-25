package com.bgsoftware.superiorprison.plugin.test.script.util;

import com.google.common.collect.Sets;
import com.google.gson.internal.Primitives;
import lombok.NonNull;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.oop.orangeengine.main.Engine.getEngine;

public class Values {
    private static final Set<Function<String, Number>> numberWrappers = Sets.newHashSet(
            in -> wrap(() -> Integer.parseInt(in)),
            in -> wrap(() -> Double.parseDouble(in))
    );

    private static <T> T wrap(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static boolean isNumber(Object object) {
        return Primitives.wrap(object.getClass()).isAssignableFrom(Number.class);
    }

    public static boolean isNumber(String value) {
        // Try to parse all possible number combinations
        for (Function<String, Number> numberWrapper : numberWrappers) {
            Number apply = numberWrapper.apply(value);
            if (apply == null) continue;

            return true;
        }

        return false;
    }

    public static Number[] parseNumbers(String... numbers) {
        Number[] parsed = new Number[numbers.length];
        for (int i = 0; i < parsed.length; i++) {
            for (Function<String, Number> numberWrapper : numberWrappers) {
                Number apply = numberWrapper.apply(numbers[i]);
                if (apply == null) continue;

                parsed[i] = apply;
            }

            if (parsed[i] == null)
                getEngine().getLogger().printError("Failed to parse {} as a number!", numbers[i]);
        }

        return parsed;
    }

    public static <T> T convertObjects(@NonNull Object object, Class<T> required) {
        Class<T> requiredWrapped = Primitives.wrap(required);

        if (object.getClass() != String.class)
            object = String.valueOf(object);

        if (required.isAssignableFrom(Number.class) && !isNumber(object))
            getEngine().getLogger().printError("Failed to do conversion for object {} to class {}, object is required to be an number, but it's not!", String.valueOf(object), required.getSimpleName());

        Object value = null;
        if (requiredWrapped == Double.class) {
            value = Double.parseDouble(object.toString());
            return (T) value;
        } else if (requiredWrapped == Integer.class) {
            value = Integer.parseInt(object.toString());
            return (T) value;
        }

        return (T) value;
    }

    public static int parseAsInt(String integer) {
        int parsed = -1;
        for (Function<String, Number> numberWrapper : numberWrappers) {
            Number apply = numberWrapper.apply(integer);
            if (apply == null) continue;

            if (apply instanceof Double)
                apply = apply.intValue();

            parsed = (int) apply;
        }

        return parsed;
    }
}
