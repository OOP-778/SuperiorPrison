package com.bgsoftware.superiorprison.plugin.config.backpack;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.Testing;
import com.bgsoftware.superiorprison.plugin.test.requirement.RequirementHolder;
import com.bgsoftware.superiorprison.plugin.test.requirement.RequirementMigrator;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BackPackUpgrade<T extends BackPackConfig<T>> {
    private List<String> description = new ArrayList<>();
    private final RequirementHolder requirementHolder;
    private final T config;

    private GlobalVariableMap variableMap = new GlobalVariableMap();

    public BackPackUpgrade(ConfigSection section, T config) {
        this.config = config;

        variableMap.newOrPut("prisoner", () -> VariableHelper.createNullVariable(SPrisoner.class));
        variableMap.newOrPut("level", () -> VariableHelper.createVariable(1));

        section.ifValuePresent("description", List.class, desc -> this.description = desc);

        RequirementMigrator.migrate(section);
        requirementHolder = Testing.controller.initializeRequirementsSection(section.getSection("requirements").get(), variableMap);
    }
}
