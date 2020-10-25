package com.bgsoftware.superiorprison.plugin.test.generator;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.Testing;
import com.bgsoftware.superiorprison.plugin.test.requirement.RequirementHolder;
import com.bgsoftware.superiorprison.plugin.test.script.util.Data;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.YamlMessage;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ObjectTemplate {
    private static final Data data = new Data();

    static {
        data.add("prisoner", SPrisoner.class);
    }

    private String prefix;
    private List<String> commands = new ArrayList<>();
    private OMessage message;
    private RequirementHolder requirements;

    private ObjectTemplate() {
    }

    public ObjectTemplate(ConfigSection section, GlobalVariableMap map) {
        this.prefix = section.getAs("prefix");
        section.ifValuePresent("commands", List.class, list -> commands.addAll(list));

        // Check for message
        if (section.isSectionPresent("message")) {
            message = YamlMessage.load(section.getSection("message").get());

        } else if (section.isValuePresent("message"))
            message = new OChatMessage(section.getAs("message", String.class));

        requirements = Testing.controller.initializeRequirementsSection(section.getSection("requirements").get(), map);
    }

    public ObjectTemplate clone() {
        ObjectTemplate clone = new ObjectTemplate();
        clone.prefix = prefix;
        clone.commands = new ArrayList<>(commands);
        clone.message = message.clone();
        clone.requirements = requirements.clone();
        return clone;
    }

    public void initialize(GlobalVariableMap map) {
        this.prefix = map.initializeVariables(prefix, data);
        this.commands = this.commands.stream().map(cmd -> map.initializeVariables(cmd, data)).collect(Collectors.toList());
        this.message = (OMessage) this.message
                .replace(in -> map.initializeVariables(in.toString(), data));

    }
}
