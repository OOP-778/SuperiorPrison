package com.bgsoftware.superiorprison.plugin.object.player;

import com.bgsoftware.superiorprison.api.data.player.PrisonerRank;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.oop.orangeengine.yaml.ConfigurationSection;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class SRank implements PrisonerRank {

    private String name;
    private String permission;
    private boolean defaultRank = false;
    private String prefix;

    private List<String> commands = new ArrayList<>();
    private Set<RequirementData> requirements;

    public SRank(String defaultPrefix, String defaultPermission, ConfigurationSection section, Set<RequirementData> requirements) {
        this.name = section.getKey();
        this.requirements = requirements;

        // Init permission
        if (section.isPresentValue("permission"))
            permission = section.getValueAsReq("permission");

        else
            permission = defaultPermission.replace("%rank_name%", name);

        // Init Prefix
        if (section.isPresentValue("prefix"))
            prefix = section.getValueAsReq("prefix");

        else
            prefix = defaultPrefix.replace("%rank_name%", name);

        section.ifValuePresent("commands", List.class, cmds -> commands.addAll((List<String>) cmds));
    }

}
