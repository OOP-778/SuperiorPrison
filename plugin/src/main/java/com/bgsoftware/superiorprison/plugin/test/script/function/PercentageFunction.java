package com.bgsoftware.superiorprison.plugin.test.script.function;

import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.Variable;
import com.google.common.base.Preconditions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper.createVariable;


public class PercentageFunction implements Function<Number> {
    public static final Pattern PATTERN = Pattern.compile("([0-9]+V|[0-9]+)(?: ?(?:%|percent)) of ([0-9]+V|[0-9]+)");

    private Variable<Number> percent;
    private Variable<Number> number;

    @Override
    public void initialize(String string, GlobalVariableMap variableMap) {
        Matcher matcher = PATTERN.matcher(string);
        matcher.find();

        String stringPercent = matcher.group(1);
        String stringNumber = matcher.group(2);

        if (!Values.isNumber(stringPercent))
            percent = variableMap.getRequiredVariableById(stringPercent, Number.class);
        else
            percent = createVariable(Values.parseAsInt(stringPercent));

        System.out.println(stringNumber);
        if (!Values.isNumber(stringNumber))
            number = variableMap.getRequiredVariableById(stringNumber, Number.class);
        else
            number = createVariable(Values.parseAsInt(stringNumber));

        Preconditions.checkArgument(percent != null, "Failed to initialize RandomNumberFunction cause `from` is null!");
        Preconditions.checkArgument(number != null, "Failed to initialize RandomNumberFunction cause `to` is null!");
    }

    @Override
    public Class<Number> getType() {
        return Number.class;
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    public Number execute(GlobalVariableMap globalVariables) {
        int percentage = percent.get(globalVariables).intValue();
        int number = this.number.get(globalVariables).intValue();

        Preconditions.checkArgument(percentage <= 100, "Percentage cannot be higher than 100!");
        return Math.round((float) number / 100 * percentage);
    }

    @Override
    public String getId() {
        return "percentage";
    }
}
