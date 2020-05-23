package com.bgsoftware.superiorprison.plugin.config;

import com.oop.orangeengine.command.ColorScheme;
import com.oop.orangeengine.yaml.ConfigSection;

public class CommandColorsSection {

    private final ColorScheme scheme;

    public CommandColorsSection(ConfigSection section) {
        scheme = new ColorScheme();
        section.ifValuePresent("main", String.class, scheme::setMainColor);
        section.ifValuePresent("second", String.class, scheme::setSecondColor);
        section.ifValuePresent("markup", String.class, scheme::setMarkupColor);
    }

    public ColorScheme getScheme() {
        return scheme;
    }
}
