package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;

import java.util.Set;

public class RankController {

    public RankController() {
        OConfiguration ranksConfig = SuperiorPrisonPlugin.getInstance().getConfigController().getRanksConfig();

        String defaultPerm = ranksConfig.getValueAsReq("default permission");

    }

}
