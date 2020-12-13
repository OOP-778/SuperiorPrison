package com.bgsoftware.superiorprison.plugin.config.main;

import com.bgsoftware.superiorprison.plugin.util.configwrapper.SectionWrapper;
import lombok.Getter;

@Getter
public class PrisonerDefaults extends SectionWrapper {

    private boolean autoSell = false;
    private boolean autoPickup = false;
    private boolean autoBurn = false;
    private boolean fortuneBlocks = false;
    private String rank;
    private String prestige;

    @Override
    protected void initialize() {
        addDefault("auto sell", false, "Is auto sell enabled");
        addDefault("auto pickup", false, "Is auto pickup enabled");
        addDefault("auto burn", false, "Is auto burn enabled");
        addDefault("fortune blocks", false, "Is fortune blocks enabled");
        addDefault("rank", "A", "Default rank");
        addDefault("prestige", "-1", "Default Prestige", "-1 means none");
        super.initialize();

        this.autoSell = getSection().getAs("auto sell");
        this.autoPickup = getSection().getAs("auto pickup");
        this.autoBurn = getSection().getAs("auto burn");
        this.fortuneBlocks = getSection().getAs("fortune blocks");
        this.rank = getSection().getAs("rank");
        this.prestige = getSection().getAs("prestige");
    }
}
