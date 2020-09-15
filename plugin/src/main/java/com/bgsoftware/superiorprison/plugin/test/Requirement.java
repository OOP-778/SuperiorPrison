package com.bgsoftware.superiorprison.plugin.test;

import com.oop.orangeengine.main.util.data.pair.OPair;

public abstract class Requirement<T extends RequirementData> {

    private OPair<String, Class>[] valuesArray;
    public Requirement(OPair<String, Class>[] valuesArray) {
        this.valuesArray = valuesArray;
    }

    public abstract String getId();
}
