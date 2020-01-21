package com.bgsoftware.superiorprison.plugin.config.main;

import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
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

    private OPair<ResetSettings.Type, String> resetting = new OPair<>(ResetSettings.Type.PERCENTAGE, "50");
    private List<OPair<Double, OMaterial>> materials;

    private List<OPair<OMaterial, Double>> shopPrices;

    public MineDefaultsSection(ConfigurationSection section) {
        this.icon = new OItem().load(section.getSection("icon"));
        this.limit = section.getValueAsReq("limit");

        ConfigurationSection resettingSection = section.getSection("resetting");
        this.resetting.set(ResetSettings.Type.valueOf(resettingSection.getValueAsReq("mode", String.class).toUpperCase()), resettingSection.getValueAsReq("value"));

        this.materials = ((List<String>) section.getValueAsReq("materials"))
                .stream()
                .map(string -> string.split(":"))
                .map(array -> new OPair<>(Double.parseDouble(array[1]), OMaterial.matchMaterial(array[0])))
                .collect(Collectors.toList());

        this.shopPrices = !section.hasValue("shop items") ? new ArrayList<>() : ((List<String>) section.getValueAsReq("shop items"))
                .stream()
                .map(string -> string.split(":"))
                .map(array -> new OPair<>(OMaterial.matchMaterial(array[0].toUpperCase()), Double.parseDouble(array[1])))
                .collect(Collectors.toList());
    }

}
