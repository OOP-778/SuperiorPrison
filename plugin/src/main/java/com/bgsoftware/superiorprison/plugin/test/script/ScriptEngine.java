package com.bgsoftware.superiorprison.plugin.test.script;

import com.bgsoftware.superiorprison.plugin.test.script.function.Function;
import com.bgsoftware.superiorprison.plugin.test.script.function.MathFunction;
import com.bgsoftware.superiorprison.plugin.test.script.function.PercentageFunction;
import com.bgsoftware.superiorprison.plugin.test.script.function.RandomNumberFunction;
import com.bgsoftware.superiorprison.plugin.test.script.util.RegexHelper;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.Variable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ScriptEngine {
    private static ScriptEngine engine;

    static {
        new ScriptEngine();
    }

    private final Set<FunctionData> functionDataSet = new HashSet<>();

    public ScriptEngine() {
        engine = this;
        registerFunction(new RandomNumberFunction(), input -> RegexHelper.matches(input, RandomNumberFunction.RANDOM_NUMBER_PATTERN));
        registerFunction(new MathFunction(), MathFunction.TESTER);
        registerFunction(new PercentageFunction(), input -> RegexHelper.matches(input, PercentageFunction.PATTERN));
    }

    public static ScriptEngine getInstance() {
        return engine;
    }

    /**
     * Registering new function to the engine
     *
     * @param id       is the id of the function, should be unique
     * @param clazz    is the class of the function
     * @param supplier supplier of the function
     * @param tester   is the predicate that tests for the function
     */
    public <T extends Function> void registerFunction(String id, Class<T> clazz, Supplier<T> supplier, Predicate<String> tester) {
        functionDataSet.add(new FunctionData(id, clazz, supplier, tester));
    }

    /**
     * Registering function by object to the engine
     *
     * @param function the function that you're trying to register
     * @param tester   test if the input is this function
     */
    @SneakyThrows
    public <T extends Function> void registerFunction(T function, Predicate<String> tester) {
        registerFunction(function.getId(), function.getClass(), () -> get(function), tester);
    }

    /**
     * Initialize function from string
     *
     * @param input  is the string that you want to parse
     * @param varMap is the global data map
     * @return a parsed function
     */
    public Function<?> initializeFunction(
            @NonNull String input,
            @NonNull GlobalVariableMap varMap
    ) throws IllegalStateException {
        // Check if it contains inside functions
        if (input.contains("}") && input.contains("{")) {
            final List<Group> groups = new ArrayList<>();
            final Deque<Integer> indexes = new LinkedList<>();
            for (int i = 0; i < input.toCharArray().length; i++) {
                char c = input.toCharArray()[i];

                if (c == '{')
                    indexes.add(i + 1);

                if (c == '}') {
                    int last = indexes.removeLast();
                    groups.add(new Group(last, i, input.substring(last, i)));
                }
            }

            Map<String, Integer> matchedGroups = new HashMap<>();
            for (Group group : groups) {
                String[] groupMatch = new String[]{group.function};
                matchedGroups.forEach((match, i) -> groupMatch[0] = groupMatch[0].replace(match, i + "V"));

                Function<?> function = initializeFunction(groupMatch[0], varMap);
                Variable<?> variable = new Variable() {
                    @Override
                    public Class getType() {
                        return function.getType();
                    }

                    @Override
                    public Object get(GlobalVariableMap globalVariableMap) {
                        return function.execute(globalVariableMap);
                    }
                };

                GlobalVariableMap.VariableData variableData = varMap.newVariable("{" + groupMatch[0] + "}", variable);
                matchedGroups.put(variableData.getInput(), variableData.getId());
            }

            for (Map.Entry<String, Integer> entry : matchedGroups.entrySet()) {
                String match = entry.getKey();
                Integer i = entry.getValue();
                input = input.replace(match, i + "V");
            }
        }

        // Try to find function by the input
        @NonNull String finalInput = input;
        FunctionData functionData = functionDataSet
                .stream()
                .filter(fd -> fd.tester.test(finalInput))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to find function for provided input " + finalInput));

        Function function = (Function) functionData.supplier.get();
        function.initialize(input, varMap);

        return function;
    }

    @SneakyThrows
    private <T extends Function> T get(Function clazz) {
        return (T) clazz.getClass().newInstance();
    }

    @AllArgsConstructor
    @Getter
    public static class Group {
        private final int start;
        private final int end;
        private final String function;
    }

    @AllArgsConstructor
    @Getter
    private class FunctionData<T extends Function> {
        private final String id;
        private final Class<T> funcClass;
        private final Supplier<T> supplier;
        private final Predicate<String> tester;
    }
}
