package com.bgsoftware.superiorprison.plugin.test.requirement;

import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.oop.orangeengine.yaml.ConfigSection;

public class RequirementController {
    public RequirementHolder initializeRequirementsSection(ConfigSection requirementsSection, GlobalVariableMap map) {
        RequirementHolder holder = new RequirementHolder(map);
        if (requirementsSection == null) return holder;

        for (ConfigSection reqSection : requirementsSection.getSections().values()) {
            holder.add(new RequirementData(reqSection, map), new Requirement());
        }

        return holder;
    }
}
