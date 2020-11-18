package com.bgsoftware.superiorprison.plugin.requirement;

import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.oop.orangeengine.yaml.ConfigSection;

public class RequirementController {
    public static RequirementHolder initializeRequirementsSection(ConfigSection requirementsSection, GlobalVariableMap map) {
        RequirementHolder holder = new RequirementHolder();
        if (requirementsSection == null) return holder;

        for (ConfigSection reqSection : requirementsSection.getSections().values()) {
            holder.add(new RequirementData(reqSection, map), new Requirement());
        }

        return holder;
    }
}
