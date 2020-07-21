package com.bgsoftware.superiorprison.plugin.config;

import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

@Getter
public class ProgressionScaleSection {

    private String symbols;
    private String color;
    private String completedColor;

    public ProgressionScaleSection(ConfigSection section) {
        this.symbols = section.getAs("symbols");
        this.color = section.getAs("color");
        this.completedColor = section.getAs("completed color");
    }
}
