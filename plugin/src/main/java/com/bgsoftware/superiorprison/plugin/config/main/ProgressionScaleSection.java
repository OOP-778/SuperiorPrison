package com.bgsoftware.superiorprison.plugin.config.main;

import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

@Getter
public class ProgressionScaleSection {

    private final String symbols;
    private final String color;
    private final String completedColor;

    public ProgressionScaleSection(ConfigSection section) {
        this.symbols = section.getAs("symbols");
        this.color = section.getAs("color");
        this.completedColor = section.getAs("completed color");
    }
}
