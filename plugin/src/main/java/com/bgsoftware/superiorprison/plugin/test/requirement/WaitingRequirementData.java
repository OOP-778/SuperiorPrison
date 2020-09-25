package com.bgsoftware.superiorprison.plugin.test.requirement;

import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

@Getter
public class WaitingRequirementData extends RequirementData {
    public ConfigSection section;

    public WaitingRequirementData(ConfigSection section) {
        super(section);
    }
}
