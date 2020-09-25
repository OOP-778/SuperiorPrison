package com.bgsoftware.superiorprison.plugin.test.script.function;

import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.Variable;
import com.google.common.base.Preconditions;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper.createVariable;

public class RandomNumberFunction implements Function<Number> {
    public static final Pattern RANDOM_NUMBER_PATTERN = Pattern
            .compile("random (?:number|num) between ([0-9]+|[0-9]+V) (?:and|&) ([0-9]+|[0-9]+V)$");

    private Variable<Number> from;
    private Variable<Number> to;

    @Override
    public void initialize(String string, GlobalVariableMap variableMap) {
        // The string should be already parsed by the global variable map
        // String example: random number between (0 and 10) || (0V() and 1V())
        Matcher matcher = RANDOM_NUMBER_PATTERN.matcher(string);
        matcher.find();

        String stringFrom = matcher.group(1);
        String stringTo = matcher.group(2);

        if (!Values.isNumber(stringFrom))
            from = variableMap.getRequiredVariableById(stringFrom, Number.class);
        else
            from = createVariable(Values.parseAsInt(stringFrom));

        if (!Values.isNumber(stringTo))
            to = variableMap.getRequiredVariableById(stringTo, Number.class);
        else
            to = createVariable(Values.parseAsInt(stringTo));

        Preconditions.checkArgument(from != null, "Failed to initialize RandomNumberFunction cause `from` is null!");
        Preconditions.checkArgument(to != null, "Failed to initialize RandomNumberFunction cause `to` is null!");
    }

    @Override
    public Class<Number> getType() {
        return Number.class;
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public Number execute(GlobalVariableMap globalVariables) {
        Number number = from.get(globalVariables);
        Number number1 = to.get(globalVariables);
        Preconditions.checkArgument(number.intValue() < number1.intValue(), "Bound is lower than the origin. (" + number + " and " + number1 + ")");

        return ThreadLocalRandom.current().nextInt(number.intValue(), number1.intValue());
    }

    @Override
    public String getId() {
        return "random number";
    }
}
