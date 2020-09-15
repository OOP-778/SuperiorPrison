package com.bgsoftware.superiorprison.plugin.test.dynamicrank;

import com.bgsoftware.superiorprison.plugin.test.RequirementData;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class DynamicRankTemplate {

    @Setter
    private int order;

    private String prefix;
    private List<String> permissions = new ArrayList<>();
    private List<String> commands = new ArrayList<>();

    private List<RequirementData> requirements = new ArrayList<>();

    public DynamicRankTemplate(ConfigSection section) {
        this.prefix = section.getAs("prefix");
        section.ifValuePresent("permissions", List.class, permissions::addAll);
        section.ifValuePresent("commands", List.class, commands::addAll);
    }

    private DynamicRankTemplate() {
    }

    public DynamicRankTemplate replace(String key, Object value) {
        this.prefix = prefix.replace(key, value.toString());
        this.permissions = permissions.stream().map(in -> in.replace(key, value.toString())).collect(Collectors.toList());
        this.commands = commands.stream().map(in -> in.replace(key, value.toString())).collect(Collectors.toList());
        return this;
    }

    public DynamicRankTemplate clone() {
        DynamicRankTemplate clone = new DynamicRankTemplate();
        clone.prefix = prefix;
        clone.commands.addAll(commands);
        clone.permissions.addAll(permissions);
        clone.requirements.addAll(requirements);
        return clone;
    }
}
