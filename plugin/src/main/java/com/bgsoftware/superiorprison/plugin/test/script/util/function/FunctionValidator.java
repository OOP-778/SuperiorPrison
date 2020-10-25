package com.bgsoftware.superiorprison.plugin.test.script.util.function;

import com.bgsoftware.superiorprison.plugin.test.script.function.Function;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public class FunctionValidator {

    private final Function function;
    private final String input;

    public <T> Function<T> validateTypeOrThrow(Class<T> clazz, Predicate<Class> validator) {
        boolean test = validator.test(function.getType());
        if (!test)
            throw new IllegalStateException("Failed to validate function. Required class type is " + clazz.getSimpleName() + " found " + function.getType().getSimpleName());

        return function;
    }

    public Function get() {
        return function;
    }
}
