package com.bgsoftware.superiorprison.plugin.util.script.function;

import com.bgsoftware.superiorprison.plugin.util.script.RegexCreator;
import com.bgsoftware.superiorprison.plugin.util.script.util.RegexHelper;
import com.bgsoftware.superiorprison.plugin.util.script.util.Values;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bgsoftware.superiorprison.plugin.util.script.RegexCreator.*;

public class ConditionFunction implements Function<Boolean> {
    private static final Pattern LESS_THAN = new RegexCreator()
            .add(group -> {
                group.addVariant(VARIABLE_VARIANT);
                group.addVariant(NUMBER_VARIANT);
            })
            .add(group -> {
                group.setMatching(false);
                group.addVariant("<");
                group.addVariant("<");
            })
            .addFromClone(0)
            .compile();

    private static final Pattern MORE_THAN = new RegexCreator()
            .add(group -> {
                group.addVariant(VARIABLE_VARIANT);
                group.addVariant(NUMBER_VARIANT);
            })
            .add(group -> {
                group.setMatching(false);
                group.addVariant(">");
                group.addVariant(">");
            })
            .addFromClone(0)
            .compile();

    private static final Pattern EQUALS_OR_MORE = new RegexCreator()
            .add(group -> {
                group.addVariant(VARIABLE_VARIANT);
                group.addVariant(NUMBER_VARIANT);
            })
            .add(group -> {
                group.setMatching(false);
                group.addVariant("is equal to or more than");
                group.addVariant(">=");
                group.addVariant("=>");
                group.addVariant("is more(?: than)* or equal to");
            })
            .addFromClone(0)
            .compile();

    private static final Pattern LESS_THAN_OR_EQUALS = new RegexCreator()
            .add(group -> {
                group.addVariant(VARIABLE_VARIANT);
                group.addVariant(NUMBER_VARIANT);
            })
            .add(group -> {
                group.setMatching(false);
                group.addVariant("is equal or less to");
                group.addVariant("<=");
                group.addVariant("=<");
                group.addVariant("is less(?: than)* or equal to");
            })
            .addFromClone(0)
            .compile();

    private static final Pattern IS_EQUAL = new RegexCreator()
            .add(group -> {
                group.addVariant(VARIABLE_VARIANT);
                group.addVariant(NUMBER_VARIANT);
            })
            .add(group -> {
                group.setMatching(false);
                group.addVariant("is equal to");
                group.addVariant("==");
            })
            .addFromClone(0)
            .compile();

    private static final List<Pattern> patternList = Arrays.asList(LESS_THAN, MORE_THAN, EQUALS_OR_MORE, LESS_THAN_OR_EQUALS, IS_EQUAL);

    private int whatId;
    private int toId;
    private int matchId;

    @Override
    public void initialize(String string, GlobalVariableMap variableMap) {
        Pattern[] patterns = patternList.toArray(new Pattern[0]);
        for (int i = 0; i < patterns.length; i++) {
            Pattern pattern = patterns[i];
            Matcher matcher = pattern.matcher(string);
            if (!matcher.find()) continue;

            matchId = i;

            String _what = matcher.group(1);
            String _to = matcher.group(2);

            whatId = VariableHelper.getElseCreate(_what, variableMap).getId();
            toId = VariableHelper.getElseCreate(_to, variableMap).getId();
        }
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    public Boolean execute(GlobalVariableMap globalVariables) {
        Object whatObject = VariableHelper.getVariable(whatId, globalVariables).getVariable().get(globalVariables);
        Object toObject = VariableHelper.getVariable(toId, globalVariables).getVariable().get(globalVariables);

        return check(whatObject, toObject);
    }

    private boolean check(Object whatObject, Object toObject) {
        if (whatObject instanceof Number && toObject instanceof Number) {
            if (matchId == 0)
                return ((Number) whatObject).doubleValue() < ((Number) toObject).doubleValue();
            else if (matchId == 1)
                return ((Number) whatObject).doubleValue() > ((Number) toObject).doubleValue();
            else if (matchId == 2)
                return ((Number) whatObject).doubleValue() >= ((Number) toObject).doubleValue();
            else if (matchId == 3)
                return ((Number) whatObject).doubleValue() <= ((Number) toObject).doubleValue();
            else if (matchId == 4)
                return ((Number) whatObject).doubleValue() == ((Number) toObject).doubleValue();
        }

        if (whatObject instanceof String || toObject instanceof String) {
            if (Values.isNumber(whatObject.toString()))
                whatObject = Values.parseAsInt(whatObject.toString());

            if (Values.isNumber(toObject.toString()))
                toObject = Values.parseAsInt(toObject.toString());

            if (whatObject instanceof Number && toObject instanceof Number)
                return check(whatObject, toObject);
        }

        return Values.compare(whatObject, toObject);
    }

    @Override
    public String getId() {
        return "BasicCondition";
    }

    public static boolean matches(String input) {
        return RegexHelper.matchFirst(input, patternList) != -1;
    }
}
