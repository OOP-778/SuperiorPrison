package com.bgsoftware.superiorprison.plugin.test.script.variable;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.test.script.util.RegexHelper;
import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Primitives;
import org.bukkit.OfflinePlayer;

import java.util.function.Function;
import java.util.function.Predicate;

public class VariableHelper {
    public static <T> Variable<T> createVariable(T object) {
        return new WrappedVariable<>(
                new Variable<T>() {
                    @Override
                    public Class<T> getType() {
                        return (Class<T>) object.getClass();
                    }

                    @Override
                    public T get(GlobalVariableMap globalVariableMap) {
                        return object;
                    }
                }
        );
    }

    public static <T> Variable<T> createNullVariable(Class<T> type) {
        return new WrappedVariable<>(
                new Variable<T>() {
                    @Override
                    public Class<T> getType() {
                        return type;
                    }

                    @Override
                    public T get(GlobalVariableMap globalVariableMap) {
                        return null;
                    }
                }
        );
    }

    public static GlobalVariableMap.VariableData getVariable(Object id, GlobalVariableMap map) {
        return getVariableAndMakeSure(id, map, vd -> true, vd -> "");
    }

    public static GlobalVariableMap.VariableData getVariableAndMakeSure(
            Object _input,
            GlobalVariableMap variableMap,
            Predicate<GlobalVariableMap.VariableData> filter,
            Function<GlobalVariableMap.VariableData, String> error
    ) {
        GlobalVariableMap.VariableData data = null;

        if (_input instanceof String) {
            // Check if string is a number
            if (Values.isNumber(_input.toString()))
                return getVariableAndMakeSure(Values.parseAsInt(_input.toString()), variableMap, filter, error);

            data = variableMap.getVariableByInput(_input.toString()).orElse(null);

            if (data == null) {
                _input = ((String) _input).replaceAll("[^\\d.]", "");
                if (_input.toString().length() > 0) {
                    if (Values.isNumber(_input.toString()))
                        return getVariableAndMakeSure(Values.parseAsInt(_input.toString()), variableMap, filter, error);
                }
            }
        }

        // Check if input is number
        if (_input instanceof Number)
            data = variableMap.getVariableDataById(((Number) _input).intValue());

        Preconditions.checkArgument(data != null, "Invalid variable by input: " + _input);
        Preconditions.checkArgument(filter.test(data), error.apply(data));

        return data;
    }

    public static GlobalVariableMap.VariableData getVariableAsNumber(
            Object id,
            GlobalVariableMap variableMap
    ) {
        return getVariableAndMakeSure(
                id,
                variableMap,
                in -> {
                    Class<?> unwrap = Primitives.wrap(in.getVariable().getType());
                    return Number.class.isAssignableFrom(unwrap);
                },
                vd -> "Variable '" + vd + "' does not return a number!"
        );
    }

    public static GlobalVariableMap.VariableData getVariableAsOfflinePlayer(
            Object _input,
            GlobalVariableMap variableMap
    ) {
        return
                getVariableAndMakeSure(
                        _input,
                        variableMap,
                        vd -> Prisoner.class.isAssignableFrom(vd.getVariable().getType()) || OfflinePlayer.class.isAssignableFrom(vd.getVariable().getType()),
                        vd -> "Variable '" + vd + "' does not return a player!"
                );
    }

    public static GlobalVariableMap.VariableData getElseCreateAsNum(String input, GlobalVariableMap variableMap) {
        Preconditions.checkArgument(input != null, "Input cannot be null!");
        try {
            if (!Values.isNumber(input))
                return getVariableAsNumber(RegexHelper.removeNonNumberAndParse(input), variableMap);
            else
                return variableMap.newOrPut(input, () -> createVariable(Values.parseAsInt(input)));
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to find or parse variable as number from '" + input + "'");
        }
    }

    public static GlobalVariableMap.VariableData getElseCreate(String input, GlobalVariableMap variableMap) {
        Preconditions.checkArgument(input != null, "Input cannot be null!");
        try {
            if (!Values.isNumber(input))
                return getVariable(RegexHelper.removeNonNumberAndParse(input), variableMap);
            else
                return variableMap.newOrPut(input, () -> createVariable(Values.parseAsInt(input)));
        } catch (Throwable throwable) {
            return variableMap.newOrPut(input, () -> createVariable(input));
        }
    }
}
