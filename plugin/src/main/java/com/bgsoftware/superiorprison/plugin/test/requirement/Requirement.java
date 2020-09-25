package com.bgsoftware.superiorprison.plugin.test.requirement;

import com.oop.orangeengine.main.util.data.pair.OPair;

public abstract class Requirement<T extends RequirementData> {

    private final OPair<String, Class>[] valuesArray;

    public Requirement(OPair<String, Class>[] valuesArray) {
        this.valuesArray = valuesArray;
    }

    public abstract String getId();

    public abstract Class<T> getDataClass();
}
