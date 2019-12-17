package com.bgsoftware.superiorprison.plugin.config.minerewards;

import com.google.common.collect.Sets;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.YamlMessage;
import com.oop.orangeengine.yaml.ConfigurationSection;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class MineReward {

    private Set<OPair<OMaterial, Long>> requirements = Sets.newHashSet();
    private OMessage message;
    private List<String> commands = new ArrayList<>();

    public MineReward(ConfigurationSection section) {
        for (String stringReq : (List<String>) section.getValueAsReq("requirements", List.class)) {
            String[] split = stringReq.split(";");

            requirements.add(new OPair<>(OMaterial.matchMaterial(split[0]), Long.parseLong(split[1])));
        }

        this.commands.addAll(section.getValueAsReq("commands"));
    }

}
