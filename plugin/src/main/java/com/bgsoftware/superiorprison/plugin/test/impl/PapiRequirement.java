package com.bgsoftware.superiorprison.plugin.test.impl;

import com.bgsoftware.superiorprison.plugin.test.requirement.Requirement;
import com.oop.orangeengine.main.util.data.pair.OPair;

public class PapiRequirement extends Requirement<PapiRequirementData> {
    public PapiRequirement() {
        super(
                new OPair[]{
                        new OPair("value", String.class),
                        new OPair("placeholder", String.class)
                }
        );
    }

    @Override
    public String getId() {
        return "papi";
    }

    @Override
    public Class<PapiRequirementData> getDataClass() {
        return PapiRequirementData.class;
    }
}
