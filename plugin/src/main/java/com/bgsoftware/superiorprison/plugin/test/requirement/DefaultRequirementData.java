package com.bgsoftware.superiorprison.plugin.test.requirement;

import com.bgsoftware.superiorprison.plugin.test.script.function.Function;
import com.oop.orangeengine.yaml.ConfigSection;

public class DefaultRequirementData extends RequirementData {
    private Function<Object> valueFunction;

    public DefaultRequirementData(ConfigSection section) {
        super(section);
    }
}
