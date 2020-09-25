package com.bgsoftware.superiorprison.plugin.test.requirement;

import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

@Getter
public abstract class RequirementData {
    private boolean take = false;
    private final String id;

    public RequirementData(ConfigSection section) {
        section.ensureHasValues("type");
        this.id = section.getKey();
        section.ifValuePresent("take", boolean.class, bool -> this.take = bool);
    }
}
