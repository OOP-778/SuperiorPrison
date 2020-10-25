package com.bgsoftware.superiorprison.plugin.test.script.function;

import com.bgsoftware.superiorprison.plugin.test.script.util.RegexHelper;
import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.Variable;
import com.google.common.base.Preconditions;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper.createVariable;
import static com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper.getVariableAsNumber;

public class RandomNumberFunction implements Function<Number> {
    public static final Pattern RANDOM_NUMBER_PATTERN = Pattern
            .compile("random (?:number|num) between ([0-9]+|[0-9]+V) (?:and|&) ([0-9]+|[0-9]+V)$");

    private int fromId;
    private int toId;

    @Override
    public void initialize(String string, GlobalVariableMap variableMap) {
        // The string should be already parsed by the global variable map
        // String example: random number between (0 and 10) || (0V() and 1V())
        Matcher matcher = RANDOM_NUMBER_PATTERN.matcher(string);
        matcher.find();

        String stringFrom = matcher.group(1);
        String stringTo = matcher.group(2);

        // Initialize from id
        if (!Values.isNumber(stringFrom))
            fromId = getVariableAsNumber(RegexHelper.removeNonNumberAndParse(stringFrom), variableMap).getId();
        else
            fromId = variableMap.newOrPut(stringFrom, () -> createVariable(Values.parseAsInt(stringFrom))).getId();

        // Initialize to id
        if (!Values.isNumber(stringTo))
            toId = getVariableAsNumber(RegexHelper.removeNonNumberAndParse(stringTo), variableMap).getId();
        else
            toId = variableMap.newOrPut(stringTo, () -> createVariable(Values.parseAsInt(stringTo))).getId();
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
        Variable<Number> from = globalVariables.getRequiredVariableById(fromId, Number.class);
        Variable<Number> to = globalVariables.getRequiredVariableById(toId, Number.class);

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
