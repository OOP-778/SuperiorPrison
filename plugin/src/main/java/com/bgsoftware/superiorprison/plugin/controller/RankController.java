package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SRank;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.main.plugin.OComponent;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
public class RankController implements com.bgsoftware.superiorprison.api.controller.RankController, OComponent<SuperiorPrisonPlugin> {

    private boolean loaded = false;

    private Map<String, SRank> ranks = Maps.newConcurrentMap();

    private SRank defaultRank;

    public RankController(boolean first) {
        // We have to load delayed so other plugins can register requirements
        if (first) {
            new OTask()
                    .delay(TimeUnit.SECONDS, 3)
                    .runnable(this::load)
                    .execute();
        } else
            load();
    }

    @Override
    public boolean load() {
        try {
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
            defaultRank = ranks.values().stream()
                    .filter(SRank::isDefaultRank)
                    .findFirst()
                    .orElse(null);
        } catch (Throwable thrw) {
            getPlugin().getOLogger().error(thrw);
            return false;
        }

        return true;
    }

    public Optional<SRank> findRankById(String id) {
        return Optional.ofNullable(ranks.get(id));
    }
}
