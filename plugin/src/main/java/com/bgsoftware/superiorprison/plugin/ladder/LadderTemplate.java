package com.bgsoftware.superiorprison.plugin.ladder;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.requirement.RequirementController;
import com.bgsoftware.superiorprison.plugin.requirement.RequirementHolder;
import com.bgsoftware.superiorprison.plugin.util.script.util.Data;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.YamlMessage;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
public class LadderTemplate {
    public static final Data data = new Data();

    static {
        data.add("prisoner", SPrisoner.class);
    }

    private String prefix;
    private List<String> commands = new ArrayList<>();
    private OMessage message;
    private RequirementHolder requirements;

    private LadderTemplate() {
    }

    public LadderTemplate(ConfigSection section, GlobalVariableMap map) {
        section.ifValuePresent("prefix", String.class, p -> this.prefix = p);
        section.ifValuePresent("commands", List.class, list -> commands.addAll(list));

        // Check for message
        if (section.isSectionPresent("message")) {
            message = YamlMessage.load(section.getSection("message").get());

        } else if (section.isValuePresent("message"))
            message = new OChatMessage(section.getAs("message", String.class));

        requirements = RequirementController.initializeRequirementsSection(section.getSection("requirements").orElse(null), map);
    }

    public LadderTemplate clone() {
        LadderTemplate clone = new LadderTemplate();
        clone.prefix = prefix;
        clone.commands = new ArrayList<>(commands);
        clone.message = message != null ? message.clone() : null;
        clone.requirements = requirements.clone();
        return clone;
    }

    public void initialize(GlobalVariableMap map) {
        this.prefix = map.initializeVariables(Objects.requireNonNull(prefix, "Prefix is not initialized!"), data);
        this.commands = this.commands.stream().map(cmd -> map.initializeVariables(cmd, data)).collect(Collectors.toList());
        if (this.message != null)
            this.message = (OMessage) this.message
                    .replace(in -> map.initializeVariables(in.toString(), data));
    }
}
