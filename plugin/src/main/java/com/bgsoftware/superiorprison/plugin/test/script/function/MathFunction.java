package com.bgsoftware.superiorprison.plugin.test.script.function;

import com.bgsoftware.superiorprison.plugin.test.script.math.MathTester;
import com.bgsoftware.superiorprison.plugin.test.script.math.parser.Parser;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;

import java.util.List;
import java.util.function.Predicate;

public class MathFunction implements Function<Number> {
    public static final Predicate<String> TESTER = input -> {
        List<MathTester.GroupsData> parse = MathTester.parse(input);
        if (parse.isEmpty()) return false;

        for (MathTester.GroupsData groupsData : parse) {
            if (MathTester.validateMath(groupsData))
                return true;
        }

        return false;
    };

    private String string;

    @Override
    public void initialize(String string, GlobalVariableMap eventData) {
        this.string = string;
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
        String stringClone = string;
        stringClone = globalVariables.extractVariables(stringClone);
        return Parser.eval(stringClone).getValue();
    }

    @Override
    public String getId() {
        return "math";
    }
}
