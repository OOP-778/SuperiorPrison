package com.bgsoftware.superiorprison.plugin.test.script.variable;

import com.google.common.base.Preconditions;

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

    public static GlobalVariableMap.VariableData getVariableAndMakeSure(
            int id,
            GlobalVariableMap variableMap,
            Predicate<GlobalVariableMap.VariableData> filter,
            Function<GlobalVariableMap.VariableData, String> error
    ) {
        GlobalVariableMap.VariableData variableDataById = variableMap.getVariableDataById(id);
        System.out.println("clazz: " + variableDataById.getVariable().getType());
        Preconditions.checkArgument(filter.test(variableDataById), error.apply(variableDataById));

        return variableDataById;
    }

    public static GlobalVariableMap.VariableData getVariableAsNumber(
            int id,
            GlobalVariableMap variableMap
    ) {
        return getVariableAndMakeSure(
                id,
                variableMap,
                in -> Number.class.isAssignableFrom(in.getVariable().getType()),
                vd -> "Variable '" + vd + "' does not return a number!"
        );
    }
}
