package com.bgsoftware.superiorprison.plugin.config;

import com.bgsoftware.superiorprison.plugin.util.configwrapper.SectionWrapper;
import lombok.Getter;

@Getter
public class PrisonerDefaults extends SectionWrapper {

    private boolean autoSell = false;
    private boolean autoPickup = false;
    private boolean autoBurn = false;
    private boolean fortuneBlocks = false;

    @Override
    protected void initialize() {
        addDefault("auto sell", false, "Is auto sell enabled");
        addDefault("auto pickup", false, "Is auto pickup enabled");
        addDefault("auto burn", false, "Is auto burn enabled");
        addDefault("fortune blocks", false, "Is fortune blocks enabled");
        super.initialize();

        autoSell = getSection().getAs("auto sell");
        autoPickup = getSection().getAs("auto pickup");
        autoBurn = getSection().getAs("auto burn");
        fortuneBlocks = getSection().getAs("fortune blocks");
    }
}
