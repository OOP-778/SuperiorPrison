package com.bgsoftware.superiorprison.plugin.config.main;

import com.oop.orangeengine.command.ColorScheme;
import com.oop.orangeengine.yaml.ConfigurationSection;

public class CommandColorsSection {

    private ColorScheme scheme;

    public CommandColorsSection(ConfigurationSection section) {
        scheme = new ColorScheme();
        section.ifValuePresent("main", String.class, scheme::setMainColor);
        section.ifValuePresent("second", String.class, scheme::setSecondColor);
        section.ifValuePresent("markup", String.class, scheme::setMarkupColor);
    }

    public ColorScheme getScheme() {
        return scheme;
    }
}
