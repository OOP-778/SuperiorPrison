package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrestige;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.main.plugin.OComponent;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PrestigeController implements com.bgsoftware.superiorprison.api.controller.PrestigeController, OComponent<SuperiorPrisonPlugin> {
    @Getter
    private boolean loaded = false;

    private Map<Integer, SPrestige> prestigeMap = Maps.newConcurrentMap();

    public PrestigeController(boolean first) {
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
    public List<Prestige> getPrestiges() {
        return prestigeMap.values()
                .stream()
                .map(prestige -> (Prestige) prestige)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Prestige> getPrestige(String name) {
        return getPrestiges()
                .stream()
                .filter(prestige -> prestige.getName().contentEquals(name))
                .findFirst();
    }

    @Override
    public Optional<Prestige> getPrestige(int order) {
        return Optional.ofNullable(prestigeMap.get(order));
    }

    @Override
    public boolean load() {
        prestigeMap.clear();
        try {
            Config prestigeConfig = SuperiorPrisonPlugin.getInstance().getConfigController().getPrestigesConfig();
            String defaultPrefix = prestigeConfig.getAs("default prefix");
            RequirementController rc = SuperiorPrisonPlugin.getInstance().getRequirementController();

            for (ConfigSection section : prestigeConfig.getSections().values()) {
                if (section.isValuePresent("order")) {
                    Set<RequirementData> reqs = Sets.newHashSet();
                    section.ifValuePresent("requirements", List.class, list -> {
                        for (Object o : list) {
                            if (o.toString().trim().length() == 0) continue;
                            OPair<String, Optional<RequirementData>> data = rc.parse(o.toString());
                            if (data.getSecond().isPresent())
                                reqs.add(data.getSecond().get());

                            else
                                getPlugin().getOLogger().printWarning("Failed to find rankup requirement by id: " + data.getFirst() + " in " + section.getKey() + " rank!");
                        }
                    });

                    int order = section.getAs("order");
                    SPrestige prestige = new SPrestige(
                            section.getKey(),
                            section.isValuePresent("prefix") ? section.getAs("prefix", String.class) : defaultPrefix.replace("{prestige_name}", section.getKey()),
                            order,
                            section.isValuePresent("commands") ? (List<String>) section.getAs("commands", List.class) : new ArrayList<>(),
                            section.isValuePresent("permissions") ? (List<String>) section.getAs("permissions", List.class) : new ArrayList<>(),
                            reqs,
                            null,
                            null
                    );
                    if (prestigeMap.containsKey(order)) {
                        getPlugin().getOLogger().printWarning("Prestige with order " + order + " already exists, aborting prestige creation!");
                        continue;
                    }
                    prestigeMap.put(order, prestige);
                }

                prestigeMap.forEach((order, prestige) -> {
                    int previous = order - 1;
                    int next = order + 1;

                    Optional.ofNullable(prestigeMap.get(previous)).ifPresent(prestige::setPreviousPrestige);
                    Optional.ofNullable(prestigeMap.get(next)).ifPresent(prestige::setNextPrestige);
                });
            }

            loaded = true;
        } catch (Throwable thrw) {
            throw new IllegalStateException("Failed to load PrestigeController", thrw);
        }
        return true;
    }
}
