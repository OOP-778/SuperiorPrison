package com.bgsoftware.superiorprison.plugin.config;

import com.bgsoftware.superiorprison.api.data.top.TopSystem;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.bgsoftware.superiorprison.plugin.util.configwrapper.SectionWrapper;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class TopSystemsSection extends SectionWrapper {

    private Map<String, TopSystemConfig> configs = new HashMap<>();

    @Override
    protected void initialize() {
        initDefault("blocks");
        initDefault("prestige");
    }

    public TopSystemConfig getConfig(String path) {
        return configs.get(path.toLowerCase());
    }

    private void initDefault(String path) {
        if (getSection().isSectionPresent(path))
            configs.put(path.toLowerCase(), new TopSystemConfig(getSection().getSection(path).get()));
        else {
            ConfigSection section = getSection().createSection(path);
            section.set("entries limit", 10);
            section.set("interval", "10s");
            configs.put(path.toLowerCase(), new TopSystemConfig(section));
        }
    }

    @Getter
    public class TopSystemConfig {
        private int limit;
        private long interval;

        public TopSystemConfig(ConfigSection section) {
            this.limit = section.getAs("entries limit");
            this.interval = TimeUtil.toSeconds(section.getAs("interval", String.class));
        }
    }
}
