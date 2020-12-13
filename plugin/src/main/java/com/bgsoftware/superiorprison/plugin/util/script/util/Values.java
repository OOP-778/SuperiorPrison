package com.bgsoftware.superiorprison.plugin.util.script.util;

import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.gson.internal.Primitives;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.oop.orangeengine.main.Engine.getEngine;

public class Values {
    private static final Set<Function<String, Number>> numberWrappers = Sets.newHashSet(
            in -> wrap(() -> Ints.tryParse(in)),
            in -> wrap(() -> Doubles.tryParse(in)),
            in -> wrap(() -> new BigInteger(in)),
            in -> wrap(() -> new BigDecimal(in))
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

    public static Number[] parseNumbersOrNull(String... numbers) {
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

    public static BigInteger parseAsBigInt(String value) {
        Number number = parseNumbers(value)[0];
        Preconditions.checkArgument(number != null, "Invalid number " + value);

        if (number instanceof BigInteger)
            return (BigInteger) number;

        return BigInteger.valueOf(number.longValue());
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

            if (apply instanceof Number)
                apply = apply.intValue();

            parsed = (int) apply;
        }

        return parsed;
    }

    public static boolean isBool(Class clazz) {
        return clazz == Boolean.class || clazz == boolean.class;
    }

    public static Boolean[] parseBooleansOrNulls(String... strings) {
        Boolean[] array = new Boolean[strings.length];
        for (int i = 0; i < strings.length; i++) {
            String atI = strings[i];
            if (atI.equalsIgnoreCase("true") || atI.equalsIgnoreCase("false"))
                array[i] = Boolean.parseBoolean(atI.toLowerCase());
        }

        return array;
    }

    public static boolean compare(Object what, Object to) {
        // Check if they are strings
        if (what instanceof String && to instanceof String) {
            // Try to parse them as numbers
            Number[] numbers = parseNumbers(what.toString(), to.toString());
            Number numberOne = numbers[0];
            Number numberTwo = numbers[1];

            // Check if they are numbers
            if (numberOne != null && numberTwo != null)
                return compare(numberOne, numberTwo);

            Boolean[] bools = parseBooleansOrNulls(what.toString(), to.toString());
            Boolean boolOne = bools[0];
            Boolean boolTwo = bools[1];

            if (boolOne != null && boolTwo != null)
                return boolOne == boolTwo;

            return what.toString().equalsIgnoreCase(to.toString());
        }

        if (what instanceof Number && to instanceof Number)
            return compareNumber((Number) what, (Number) to) >= 0;

        return false;
    }

    public static int compareNumber(Number x, Number y) {
        if(isSpecial(x) || isSpecial(y))
            return Double.compare(x.doubleValue(), y.doubleValue());
        else
            return toBigDecimal(x).compareTo(toBigDecimal(y));
    }

    private static boolean isSpecial(Number x) {
        boolean specialDouble = x instanceof Double
                && (Double.isNaN((Double) x) || Double.isInfinite((Double) x));

        boolean specialFloat = x instanceof Float
                && (Float.isNaN((Float) x) || Float.isInfinite((Float) x));

        return specialDouble || specialFloat;
    }

    private static BigDecimal toBigDecimal(Number number) {
        if(number instanceof BigDecimal)
            return (BigDecimal) number;

        if(number instanceof BigInteger)
            return new BigDecimal((BigInteger) number);

        if(number instanceof Byte || number instanceof Short
                || number instanceof Integer || number instanceof Long)
            return new BigDecimal(number.longValue());

        if(number instanceof Float || number instanceof Double)
            return new BigDecimal(number.doubleValue());

        try {
            return new BigDecimal(number.toString());
        } catch(final NumberFormatException e) {
            throw new RuntimeException("The given number (\"" + number + "\" of class " + number.getClass().getName() + ") does not have a parsable string representation", e);
        }
    }

    public static boolean isSameClass(Class<?> v1, Class<?> v2) {
        Class<?> c1 = Primitives.unwrap(v1);
        Class<?> c2 = Primitives.unwrap(v2);
        return c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1);
    }
}
