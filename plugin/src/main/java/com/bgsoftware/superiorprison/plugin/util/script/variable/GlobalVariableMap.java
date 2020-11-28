package com.bgsoftware.superiorprison.plugin.util.script.variable;

import com.bgsoftware.superiorprison.plugin.util.script.util.Data;
import com.bgsoftware.superiorprison.plugin.util.script.util.ReflectionUtil;
import com.bgsoftware.superiorprison.plugin.util.script.util.Values;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Primitives;

import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.lib.google.gson.JsonArray;
import com.oop.datamodule.lib.google.gson.JsonObject;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobalVariableMap implements SerializableObject {
    public static final Pattern VAR_PATTERN = Pattern.compile("%([^ ]+)%");
    public static final Pattern METHOD_PATTERN = Pattern.compile("%([^ ]+#[^ ]+)%");
    public static final Pattern PARSED_VAR_PATTERN = Pattern.compile("([0-9]+)V");
    private static final Pattern BOOL_PATTERN = Pattern.compile("(true|false)");
    private AtomicInteger varId = new AtomicInteger(0);

    private final VariableStorage variableStorage = new VariableStorage();

    /*
    Initializes event variables
    */
    public String initializeVariables(String input, Data eventData) {
        Matcher matcher;

        // Initialize booleans
        matcher = BOOL_PATTERN.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);

            Optional<VariableData> variableByInput = getVariableByInput(group);
            if (variableByInput.isPresent()) {
                int id = variableByInput.get().getId();
                input = input.replace(matcher.group(), id + "V");
                continue;
            }

            VariableData var = newVariable(group, VariableHelper.createVariable(group.equalsIgnoreCase("true")));
            int id = var.id;
            input = input.replace(matcher.group(), id + "V");
        }

        // Try to initialize method variables
        matcher = METHOD_PATTERN.matcher(input);
        while (matcher.find()) {
            Optional<VariableData> variableByInput = getVariableByInput(matcher.group(1));
            if (variableByInput.isPresent()) {
                int id = variableByInput.get().getId();
                input = input.replace(matcher.group(), id + "V");
                continue;
            }

            OPair<String, String>[] pars = parseMultiVars(matcher.group(1));

            // Check if variable is inside the eventData
            Class<?> lastMethodType = eventData == null ? null : (Class<?>) eventData.get(pars[0].getKey()).orElse(null);

            if (lastMethodType == null) {
                Matcher finalMatcher = matcher;
                lastMethodType = getVariableByInput(pars[0].getKey())
                        .map(vd -> vd.getVariable().getType())
                        .orElseThrow(() -> new IllegalStateException("Failed to initialize reflection call at " + finalMatcher.group(1) + " cause unknown data type by '" + pars[0].getKey() + "'"));
            }

            Class<?> firstClassType = lastMethodType;
            Preconditions.checkArgument(lastMethodType != null, "Failed to initialize variable by " + matcher.group() + " cause it's not a valid one!");

            Method[] methods = new Method[pars.length];
            for (int i = 0; i < methods.length; i++) {
                Method method = ReflectionUtil.getMethod(lastMethodType, pars[i].getValue());

                methods[i] = method;
                lastMethodType = method.getReturnType();
            }

            Class<?> finalLastMethodType = lastMethodType;
            Variable<?> variable = new Variable() {
                @Override
                public Class<?> getType() {
                    return finalLastMethodType;
                }

                @Override
                public Object get(GlobalVariableMap globalVariableMap) {
                    Object lastObject = globalVariableMap.getRequiredVariableByInput(pars[0].getKey(), firstClassType).get(globalVariableMap);

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
            Optional<VariableData> variableByInput = getVariableByInput(matcher.group());
            if (variableByInput.isPresent())
                input = input.replace(matcher.group(), variableByInput.get().getId() + "V");
            else {
                variableByInput = getVariableByInput(matcher.group(1));
                if (variableByInput.isPresent())
                    input = input.replace(matcher.group(), variableByInput.get().getId() + "V");
                else
                    input = input.replace(matcher.group(), newVariable(matcher.group(), VariableHelper.createNullVariable(Object.class)).getId() + "V");
            }
        }

        return input;
    }

    public Optional<VariableData> getVariableByInput(String input) {
        Preconditions.checkArgument(input != null, "Input cannot be null!");
        return variableStorage.lookupByInput(input);
    }

    public <T> Variable<T> getRequiredVariableByInput(String input, Class<T> type) {
        return (Variable<T>) getVariableByInput(input)
                .map(VariableData::getVariable)
                .filter(variable -> variable.getType().isAssignableFrom(type))
                .orElseThrow(() -> new IllegalStateException("Failed to find variable by " + input + " that returns " + type.getSimpleName()));
    }

    @SneakyThrows
    public <T> Variable<T> getRequiredVariableById(int id, Class<T> type) {
        return (Variable<T>) variableStorage.lookupById(id)
                .map(VariableData::getVariable)
                .filter(v -> {
                    Class vType = v.getType();
                    boolean result = type.isAssignableFrom(Primitives.wrap(vType));
                    if (!result)
                        return false;
                    return true;
                })
                .orElseThrow(() -> new IllegalStateException("Failed to find variable by id " + id + " that returns " + type.getSimpleName()));
    }

    public VariableData getVariableDataById(int id) {
        return variableStorage.lookupById(id)
                .orElseThrow(() -> new IllegalStateException("Failed to find variable " + id));
    }

    public VariableData newVariable(String input, Variable<?> variable) {
        Preconditions.checkArgument(!getVariableByInput(input).isPresent(), "Variable already contains by input: " + input);
        if (!(variable instanceof WrappedVariable))
            variable = new WrappedVariable<>(variable);

        int id = varId.getAndIncrement();

        VariableData variableData = new VariableData(input, id, variable);
        variableStorage.insert(id, input, variableData);
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

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("var count", varId.get());
        JsonArray array = new JsonArray();

        variableStorage.byId.forEach((id, varData) -> {
            JsonObject jsonVarData = new JsonObject();
            jsonVarData.addProperty("id", id);
            jsonVarData.addProperty("input", varData.getInput());

            JsonObject variable = new JsonObject();
            variable.addProperty("type", varData.getVariable().getType().getSimpleName());
            variable.addProperty("value", varData.getVariable().get(this) + "");

            jsonVarData.add("variable", variable);
            array.add(jsonVarData);
        });

        serializedData.write("data", array);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
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
                    ", type=" + variable.getType() +
                    '}';
        }

        public String toString(GlobalVariableMap map) {
            return "VariableData{" +
                    "input='" + input + '\'' +
                    ", id=" + id +
                    ", variable=" + variable.get(map) +
                    ", type=" + variable.getType() +
                    '}';
        }
    }

    public String extractVariables(String input) {
        Matcher matcher = PARSED_VAR_PATTERN.matcher(input);
        while (matcher.find()) {
            Integer id = Ints.tryParse(matcher.group(1));
            Variable<Object> requiredVariableById = getRequiredVariableById(id, Object.class);
            input = StringUtils.replace(input, matcher.group(), Objects.requireNonNull(requiredVariableById.get(this), "Variable by id " + id + ", type: " + requiredVariableById.getType()).toString());
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
                "variables=" + Arrays.toString(variableStorage.byId.values().stream().map(vd -> vd.toString(this)).toArray()) +
                ", counter=" + varId +
                '}';
    }

    public GlobalVariableMap clone() {
        GlobalVariableMap varMap = new GlobalVariableMap();
        varMap.varId = new AtomicInteger(varId.get());
        variableStorage.byId.forEach((key, value) -> {
            varMap.variableStorage.insert(key, value.input, new VariableData(value.input, value.id, value.variable));
        });
        return varMap;
    }

    /**
     * Because of the way of storing variable data just by id, is not enough
     * We gonna have two maps for that
     */
    public static class VariableStorage {

        // Store input to variable data
        private Map<String, VariableData> byInput = Maps.newHashMap();

        // Store id to variable data
        private Map<Integer, VariableData> byId = Maps.newHashMap();

        public Optional<VariableData> lookupById(int id) {
            return Optional.ofNullable(byId.get(id));
        }

        public Optional<VariableData> lookupByInput(String input) {
            return Optional.ofNullable(byInput.get(input));
        }

        public Optional<VariableData> lookupByUnknown(String unknown) {
            // First see if it's contained within the input map
            VariableData lookup;

            lookup = byInput.get(unknown.toLowerCase(Locale.ROOT));
            if (lookup != null)
                return Optional.of(lookup);

            int i = Values.parseAsInt(unknown);
            if (i != -1) {
                lookup = byId.get(i);
                if (lookup != null)
                    return Optional.of(lookup);
            }

            return Optional.empty();
        }

        public void insert(int id, String input, VariableData variable) {
            byId.put(id, variable);
            byInput.put(input.toLowerCase(Locale.ROOT), variable);
        }
    }
}
