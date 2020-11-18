package com.bgsoftware.superiorprison.plugin.config.main;

import com.bgsoftware.superiorprison.plugin.util.configwrapper.SectionWrapper;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.ConfigValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DisabledModulesSection extends SectionWrapper {
    private List<String> disabledModules = new ArrayList<>();

    public boolean contains(String in) {
        return disabledModules.contains(in.toLowerCase(Locale.ROOT));
    }

    @Override
    protected void initialize() {
        ConfigSection section = getSection();
        addDefault("backpacks", false, "Disable backpacks functionality of SuperiorPrison");
        super.initialize();

        for (ConfigValue value : section.getValues().values())
            if ((Boolean) value.getObject())
                disabledModules.add(value.getKey().toLowerCase(Locale.ROOT));
    }
}
