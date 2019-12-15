package com.bgsoftware.superiorprison.plugin.config;

import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.yaml.ConfigurationSection;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MineDefaultsSection {

    private OItem icon;
    private int limit = -1;

    private OPair<String, String> resetting = new OPair<>("Percentage", "50");
    private List<OPair<OMaterial, Double>> materials;

    public MineDefaultsSection(ConfigurationSection section) {
        this.icon = new OItem().load(section.getSection("icon"));
        this.limit = section.getValueAsReq("limit");

        ConfigurationSection resettingSection = section.getSection("resetting");
        this.resetting.set(resettingSection.getValueAsReq("mode"), resettingSection.getValueAsReq("value"));

        this.materials = ((List<String>)section.getValueAsReq("materials")).stream()
                .map(string -> string.split(";"))
                .map(array -> new OPair<OMaterial, Double>(OMaterial.matchMaterial(array[0]), Double.valueOf(array[1])))
                .collect(Collectors.toList());


    }

}
