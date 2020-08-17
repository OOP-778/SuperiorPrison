package com.bgsoftware.superiorprison.plugin.util.configwrapper;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.Config;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public abstract class ConfigWrapper implements DefaultValues {
    @Getter
    private final Map<String, OPair<Object, String[]>> defaultValues = new HashMap<>();

    private final Map<String, SectionWrapper> sections = new HashMap<>();

    @Getter
    @Setter
    private Config config;

    public <T extends SectionWrapper> T addSection(String key, T wrapper) {
        wrapper.setSection(config.createSection(key));
        sections.put(key, wrapper);
        return wrapper;
    }

    public void initialize() {
        _init(getConfig());

        for (SectionWrapper section : sections.values())
            section.initialize();
    }
}
