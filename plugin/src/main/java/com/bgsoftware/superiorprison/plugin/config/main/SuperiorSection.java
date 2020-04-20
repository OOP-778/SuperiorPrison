package com.bgsoftware.superiorprison.plugin.config.main;

import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class SuperiorSection {

    private OConfiguration configuration;
    private ConfigurationSection section;

    @Setter
    private boolean updated = false;

    public SuperiorSection(OConfiguration currentConfig, OConfiguration newConfig) {
        this.configuration = currentConfig;

    }

    public abstract String getPath();
}
