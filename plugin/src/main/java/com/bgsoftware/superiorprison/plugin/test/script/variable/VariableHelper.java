package com.bgsoftware.superiorprison.plugin.test.script.variable;

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
}
