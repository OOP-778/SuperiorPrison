package com.bgsoftware.superiorprison.plugin.test.script.variable;

import com.bgsoftware.superiorprison.plugin.test.script.util.Data;
import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.google.common.base.Preconditions;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobalVariableMap {
    private static final Pattern VAR_PATTERN = Pattern.compile("%([^ ]+)%");
    private static final Pattern METHOD_PATTERN = Pattern.compile("%([^ ]+#[^ ]+)%");
    private static final Pattern PARSED_VAR_PATTERN = Pattern.compile("([0-9]+)V");

    private Map<Integer, VariableData> variables = new HashMap<>();
    private AtomicInteger varId = new AtomicInteger(0);

    /*
    Initializes event variables
    */
    public String initializeVariables(String input, Data eventData) {
        // Try to initialize method variables
        Matcher matcher = METHOD_PATTERN.matcher(input);
        while (matcher.find()) {
            Optional<VariableData> variableByInput = getVariableByInput(matcher.group(1));
            if (variableByInput.isPresent()) {
                int id = variableByInput.get().getId();
                input = input.replace(matcher.group(), id + "V");
                continue;
            }

            OPair<String, String>[] pars = parseMultiVars(matcher.group(1));

            // Check if variable is inside the eventData
            Class<?> lastMethodType = (Class<?>) eventData.get(pars[0].getKey()).orElse(null);
            Class<?> firstClassType = lastMethodType;
            Preconditions.checkArgument(lastMethodType != null, "Failed to initialize variable by " + matcher.group() + " cause it's not a valid one!");

            Method[] methods = new Method[pars.length];
            for (int i = 0; i < methods.length; i++) {
                Method method;
                if (i == 0)
                    method = OSimpleReflection.getMethod(lastMethodType, pars[i].getValue());
                else
                    method = OSimpleReflection.getMethod(lastMethodType, pars[i].getValue());

                methods[i] = method;
                lastMethodType = method.getReturnType();
            }

            Class<?> finalLastMethodType = lastMethodType;
            Variable<?> variable = new Variable() {
                @Override
                public Class getType() {
                    return finalLastMethodType;
                }

                @Override
                public Object get(GlobalVariableMap globalVariableMap) {
                    Object lastObject = globalVariableMap.getRequiredVariableById(pars[0].getKey(), firstClassType);
                    for (Method method : methods) {
                        try {
                            lastObject = method.invoke(lastObject);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                    return lastObject;
                }
            };

            VariableData variableData = newVariable(matcher.group(1), variable);
            input = input.replace(matcher.group(), variableData.getId() + "V");
        }

        matcher = VAR_PATTERN.matcher(input);
        while (matcher.find()) {
            Optional<VariableData> variableByInput = getVariableByInput(matcher.group(1));
            if (variableByInput.isPresent()) {
                input = input.replace(matcher.group(), variableByInput.get().getId() + "V");
                continue;
            }
        }

        return input;
    }

    public Optional<VariableData> getVariableByInput(String input) {
        return Optional.ofNullable(variables.get(variables.values().stream().filter(data -> data.input.equalsIgnoreCase(input)).findFirst().map(VariableData::getId).orElse(-1)));
    }

    public <T> Variable<T> getRequiredVariableByInput(String input, Class<T> type) {
        return (Variable<T>) getVariableByInput(input)
                .map(VariableData::getVariable)
                .filter(variable -> variable.getType().isAssignableFrom(type))
                .orElseThrow(() -> new IllegalStateException("Failed to find variable by " + input + " that returns " + type.getSimpleName()));
    }

    public <T> Variable<T> getRequiredVariableById(int id, Class<T> type) {
        return (Variable<T>) Optional.ofNullable(variables.get(id))
                .map(VariableData::getVariable)
                .filter(v -> type.isAssignableFrom(v.getType()))
                .orElseThrow(() -> new IllegalStateException("Failed to find variable by id " + id + " that returns " + type.getSimpleName()));
    }

    public <T> Variable<T> getRequiredVariableById(String id, Class<T> type) {
        return getRequiredVariableById(Values.parseAsInt(id.replaceAll("[^\\d.]", "")), type);
    }

    public Optional<VariableData> findVariableDataBy(Predicate<VariableData> filter) {
        return variables.values()
                .stream()
                .filter(filter)
                .findFirst();
    }

    public VariableData getVariableDataById(int id) {
        return findVariableDataBy(vd -> vd.id == id)
                .orElseThrow(() -> new IllegalStateException("Failed to find variable " + id));
    }

    public VariableData getVariableDataByInput(String input) {
        return findVariableDataBy(vd -> vd.input.equalsIgnoreCase(input))
                .orElseThrow(() -> new IllegalStateException("Failed to find variable " + input));
    }

    public VariableData getVariableDataById(String id) {
        return getVariableDataById(Values.parseAsInt(id.replaceAll("[^\\d.]", "")));
    }

    public VariableData newVariable(String input, Variable<?> variable) {
        Preconditions.checkArgument(!getVariableByInput(input).isPresent(), "Variable already contains by input: " + input);
        if (!(variable instanceof WrappedVariable))
            variable = new WrappedVariable<>(variable);

        int id = varId.getAndIncrement();

        VariableData variableData = new VariableData(input, id, variable);
        variables.put(id, variableData);
        return variableData;
    }

    public VariableData newOrPut(String input, Supplier<Variable<?>> supplier) {
        Optional<VariableData> variableByInput = getVariableByInput(input);
        return variableByInput.orElseGet(() -> newVariable(input, supplier.get()));
    }

    public VariableData newOrReplace(String input, Variable<?> variable) {
        Optional<VariableData> variableByInput = getVariableByInput(input);
        if (variableByInput.isPresent()) {
            VariableData variableData = variableByInput.get();
            variableData.variable = variable;

            return variableData;
        }

        return newVariable(input, variable);
    }

    @AllArgsConstructor
    @Getter
    public static class VariableData {
        private String input;
        private int id;
        private Variable<?> variable;

        @Override
        public String toString() {
            return "VariableData{" +
                    "input='" + input + '\'' +
                    ", id=" + id +
                    ", variable=" + variable +
                    '}';
        }

        public String toString(GlobalVariableMap map) {
            return "VariableData{" +
                    "input='" + input + '\'' +
                    ", id=" + id +
                    ", variable=" + variable.get(map) +
                    '}';
        }
    }

    public String extractVariables(String input) {
        Matcher matcher = PARSED_VAR_PATTERN.matcher(input);
        while (matcher.find()) {
            Variable<Object> requiredVariableById = getRequiredVariableById(Integer.parseInt(matcher.group(1)), Object.class);
            input = input.replace(matcher.group(), requiredVariableById.get(this).toString());
        }
        return input;
    }

    private static OPair<String, String>[] parseMultiVars(String group) {
        List<OPair<String, String>> parsed = new ArrayList<>();
        String[] split = group.split("#");

        int currentIndex = 0;
        while (currentIndex != split.length) {
            // The first variable
            if (currentIndex == 0) {
                parsed.add(new OPair<>(split[currentIndex], split[currentIndex + 1]));
                currentIndex++;
            } else
                parsed.add(new OPair<>(split[currentIndex - 1], split[currentIndex]));
            currentIndex++;
        }
        return parsed.toArray(new OPair[0]);
    }

    @Override
    public String toString() {
        return "GlobalVariableMap{" +
                "variables=" + Arrays.toString(variables.values().stream().map(vd -> vd.toString(this)).toArray()) +
                ", counter=" + varId +
                '}';
    }

    public GlobalVariableMap clone() {
        GlobalVariableMap varMap = new GlobalVariableMap();
        varMap.varId = new AtomicInteger(varId.get());
        variables.forEach((key, value) -> {
            varMap.variables.put(key, new VariableData(value.input, value.id, value.variable));
        });
        return varMap;
    }
}
