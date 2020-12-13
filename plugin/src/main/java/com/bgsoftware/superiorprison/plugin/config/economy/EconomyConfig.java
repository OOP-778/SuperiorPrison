package com.bgsoftware.superiorprison.plugin.config.economy;

import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.bgsoftware.superiorprison.plugin.util.configwrapper.ConfigWrapper;
import com.oop.orangeengine.yaml.Config;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class EconomyConfig extends ConfigWrapper {
    private final boolean enabled;
    private BigDecimal maxBalance;

    public EconomyConfig(Config config) {
        enabled = config.getAs("enabled", boolean.class);
        maxBalance = NumberUtil.formattedToBigDecimal(config.getAs("max balance", String.class));
    }
}
