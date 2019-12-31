package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementHandler;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.rank.SRank;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RankController implements com.bgsoftware.superiorprison.api.controller.RankController {

    @Getter
    private boolean loaded = false;

    @Getter
    private Map<String, SRank> ranks = Maps.newConcurrentMap();

    public RankController() {
        // We have to load delayed so other plugins can register requirements
        new OTask()
                .delay(TimeUnit.SECONDS, 3)
                .runnable(this::load)
                .execute();
    }

    public void load() {
        OConfiguration ranksConfig = SuperiorPrisonPlugin.getInstance().getConfigController().getRanksConfig();
        String defaultPerm = ranksConfig.getValueAsReq("default permission");
        String defaultPrefix = ranksConfig.getValueAsReq("default prefix");
        RequirementController rc = SuperiorPrisonPlugin.getInstance().getRequirementController();

        for (ConfigurationSection section : ranksConfig.getSections().values()) {
            Set<RequirementData> reqs = Sets.newHashSet();

            section.ifValuePresent("requirements", List.class, list -> {
                for (Object o : list) {
                    OPair<String, Optional<RequirementData>> data = rc.parse(o.toString());
                    if (data.getSecond().isPresent())
                        reqs.add(data.getSecond().get());

                    else
                        SuperiorPrisonPlugin.getInstance().getOLogger().printWarning("Failed to find rankup requirement by id: " + data.getFirst() + " in " + section.getKey() + " rank!");
                }
            });
            ranks.put(section.getKey(), new SRank(defaultPrefix, defaultPerm, section, reqs));
        }
    }

}
