package com.bgsoftware.superiorprison.plugin.config;

import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
public abstract class SuperiorSection {

    private final Config configuration;
    private ConfigSection section;

    @Setter
    private boolean updated = false;

    public SuperiorSection(Config currentConfig, Config newConfig) {
        this.configuration = currentConfig;
    }

    public abstract String getPath();
}
