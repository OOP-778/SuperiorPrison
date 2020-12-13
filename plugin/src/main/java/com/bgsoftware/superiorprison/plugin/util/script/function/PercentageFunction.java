package com.bgsoftware.superiorprison.plugin.util.script.function;

import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.bgsoftware.superiorprison.plugin.util.script.util.RegexHelper;
import com.bgsoftware.superiorprison.plugin.util.script.util.Values;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.Variable;
import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper.createVariable;
import static com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper.getVariableAsNumber;

public class PercentageFunction implements Function<Number> {
    public static final Pattern PATTERN = Pattern.compile("([0-9]+V|[0-9]+|-[0-9]+)(?: ?(?:%|percent)) of ([0-9]+V|[0-9]+|-[0-9]+)");

    private int percentId;
    private int numberId;

    @Override
    public void initialize(String string, GlobalVariableMap variableMap) {
        Matcher matcher = PATTERN.matcher(string);
        matcher.find();

        String stringPercent = matcher.group(1);
        String stringNumber = matcher.group(2);

        // Initialize from id
        if (!Values.isNumber(stringPercent))
            percentId = getVariableAsNumber(RegexHelper.removeNonNumberAndParse(stringPercent), variableMap).getId();
        else
            percentId = variableMap.newOrPut(stringPercent, () -> createVariable(Values.parseAsInt(stringPercent))).getId();

        // Initialize to id
        if (!Values.isNumber(stringNumber))
            numberId = getVariableAsNumber(RegexHelper.removeNonNumberAndParse(stringNumber), variableMap).getId();
        else
            numberId = variableMap.newOrPut(stringNumber, () -> createVariable(Values.parseAsInt(stringNumber))).getId();
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
        Variable<Number> percentVar = globalVariables.getRequiredVariableById(percentId, Number.class);
        Variable<Number> numberVar = globalVariables.getRequiredVariableById(numberId, Number.class);

        BigDecimal percentage = NumberUtil.convertToBigDecimal(percentVar.get(globalVariables));
        BigDecimal number = NumberUtil.convertToBigDecimal(numberVar.get(globalVariables));

        Preconditions.checkArgument(NumberUtil.isMoreThan(percentage, BigDecimal.valueOf(100)), "Percentage cannot be higher than 100!");
        return number.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).multiply(percentage).max(BigDecimal.valueOf(100)).intValue();
    }

    @Override
    public String getId() {
        return "percentage";
    }
}
