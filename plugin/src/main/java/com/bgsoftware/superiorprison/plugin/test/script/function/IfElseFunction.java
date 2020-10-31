package com.bgsoftware.superiorprison.plugin.test.script.function;

import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.Variable;
import com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper;
import com.google.common.base.Preconditions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IfElseFunction implements Function<Object> {

    public static final Pattern IF_ELSE_PATTERN = Pattern.compile("if ([0-9]+V): ([0-9]+V) else: ([0-9]+V)");

    private int checkId;
    private int case1Id;
    private int case2Id;
    private Class mightReturn;

    @Override
    public void initialize(String string, GlobalVariableMap variableMap) {
        Matcher matcher = IF_ELSE_PATTERN.matcher(string);
        matcher.find();

        checkId = VariableHelper.getVariableAndMakeSure(
                matcher.group(1),
                variableMap,
                vd -> Boolean.class.isAssignableFrom(vd.getVariable().getType()),
                (vd) -> "Failed to find variable for if else check by `" + matcher.group(1) + "`"
        ).getId();

        GlobalVariableMap.VariableData case1Data = VariableHelper.getVariable(matcher.group(2), variableMap);
        GlobalVariableMap.VariableData case2Data = VariableHelper.getVariable(matcher.group(3), variableMap);

        Preconditions.checkArgument(
                case1Data.getVariable().getType().isAssignableFrom(case2Data.getVariable().getType()),
                "Failed to initialize if else expression, cause both cases doesn't return similar objects " +
                        "case1 " + case1Data.getVariable().getType().getName() + ", case2: " + case2Data.getVariable().getType().getName()
        );

        mightReturn = case1Data.getVariable().getType();
        this.case1Id = case1Data.getId();
        this.case2Id = case2Data.getId();
    }

    @Override
    public Class<Object> getType() {
        return mightReturn;
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public Object execute(GlobalVariableMap globalVariables) {
        Variable<Boolean> checkVar = globalVariables.getRequiredVariableById(checkId, Boolean.class);
        Variable case1 = globalVariables.getVariableDataById(case1Id).getVariable();
        Variable case2 = globalVariables.getVariableDataById(case2Id).getVariable();

        Boolean aBoolean = checkVar.get(globalVariables);

        if (aBoolean)
            return case1.get(globalVariables);
        else
            return case2.get(globalVariables);
    }

    @Override
    public String getId() {
        return "if else";
    }
}
