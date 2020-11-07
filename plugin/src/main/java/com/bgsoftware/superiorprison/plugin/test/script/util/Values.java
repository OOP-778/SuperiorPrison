package com.bgsoftware.superiorprison.plugin.test.script.util;

import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.gson.internal.Primitives;
import lombok.NonNull;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.oop.orangeengine.main.Engine.getEngine;

public class Values {
    private static final Set<Function<String, Number>> numberWrappers = Sets.newHashSet(
            in -> wrap(() -> Ints.tryParse(in)),
            in -> wrap(() -> Doubles.tryParse(in))
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

    public static Number[] parseNumbersOrNull(String ...numbers) {
        Number[] parsed = new Number[numbers.length];
        for (int i = 0; i < parsed.length; i++) {
            for (Function<String, Number> numberWrapper : numberWrappers) {
                Number apply = numberWrapper.apply(numbers[i]);
                if (apply == null) continue;
                parsed[i] = apply;
            }
        }

        return parsed;
    }

    public static Number[] parseNumbers(String... numbers) {
        Number[] parsed = new Number[numbers.length];
        for (int i = 0; i < parsed.length; i++) {
            for (Function<String, Number> numberWrapper : numberWrappers) {
                Number apply = numberWrapper.apply(numbers[i]);
                if (apply == null) continue;

                parsed[i] = apply;
            }
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

    public static boolean isBool(Class clazz) {
        return clazz == Boolean.class || clazz == boolean.class;
    }

    public static boolean[] parseBooleansOrNulls(String ...strings) {
        boolean[] array = new boolean[strings.length];
        for (int i = 0; i < strings.length; i++) {
            String atI = strings[i];
            if (atI.equalsIgnoreCase("true") || atI.equalsIgnoreCase("false"))
                array[i] = Boolean.parseBoolean(atI.toLowerCase());
        }

        return array;
    }

    public static boolean compare(Object first, Object second) {
        // Check if they are strings
        if (first instanceof String && second instanceof String) {
            // Try to parse them as numbers
            Number[] numbers = parseNumbers(first.toString(), second.toString());
            Number numberOne = numbers[0];
            Number numberTwo = numbers[1];

            // Check if they are numbers
            if (numberOne != null && numberTwo != null)
                return numberOne.doubleValue() <= numberTwo.doubleValue();

            boolean[] bools = parseBooleansOrNulls(first.toString(), second.toString());
            Boolean boolOne = bools[0];
            Boolean boolTwo = bools[1];

            if (boolOne != null && boolTwo != null)
                return boolOne == boolTwo;

            return first.toString().equalsIgnoreCase(second.toString());
        }

        if (first instanceof Number && second instanceof Number)
            return ((Number) second).doubleValue() >= ((Number) first).doubleValue();

        return false;
    }

    public static boolean isSameClass(Class<?> v1, Class<?> v2) {
        Class<?> c1 = Primitives.unwrap(v1);
        Class<?> c2 = Primitives.unwrap(v2);
        return c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1);
    }
}
