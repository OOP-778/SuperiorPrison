package com.bgsoftware.superiorprison.plugin.test.impl;

import com.bgsoftware.superiorprison.plugin.test.requirement.RequirementData;
import com.bgsoftware.superiorprison.plugin.test.script.ScriptEngine;
import com.bgsoftware.superiorprison.plugin.test.script.function.Function;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.oop.orangeengine.yaml.ConfigSection;

import static com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper.createVariable;

public class PapiRequirementData extends RequirementData {

    private Function<String> placeholder;

    public PapiRequirementData(ConfigSection section, GlobalVariableMap map) {
        super(section);

        Function<?> placeholder = ScriptEngine.getInstance().initializeFunction(section.getAs("placeholder"), map);
        placeholder.execute(map);
    }
}
