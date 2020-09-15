package com.bgsoftware.superiorprison.plugin.test;

import com.oop.orangeengine.yaml.ConfigSection;

public abstract class RequirementData {

    private boolean take = false;
    private String id;

    public RequirementData(ConfigSection section) {
        section.ensureHasValues("type");
        this.id = section.getKey();
        section.ifValuePresent("take", boolean.class, bool -> this.take = bool);
    }
}
