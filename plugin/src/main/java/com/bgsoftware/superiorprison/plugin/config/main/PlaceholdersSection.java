package com.bgsoftware.superiorprison.plugin.config.main;

import com.bgsoftware.superiorprison.plugin.util.configwrapper.SectionWrapper;
import lombok.Getter;

@Getter
public class PlaceholdersSection extends SectionWrapper {

    private String rankNotFound;
    private String prestigeNotFound;

    @Override
    protected void initialize() {
        addDefault("rank not found", "N\\A", "What's displayed when rank is not found");
        addDefault("prestige not found", "N\\A", "What's displayed when prestige is not found");
        super.initialize();

        rankNotFound = getSection().getAs("rank not found");
        prestigeNotFound = getSection().getAs("prestige not found");
    }
}
