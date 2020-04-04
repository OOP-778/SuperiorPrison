package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SSpecialRank;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.main.plugin.OComponent;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RankController implements com.bgsoftware.superiorprison.api.controller.RankController, OComponent<SuperiorPrisonPlugin> {

    @Getter
    private boolean loaded = false;
    private Map<Integer, SLadderRank> ladderRanks = Maps.newConcurrentMap();
    private Set<SSpecialRank> specialRanks = Sets.newConcurrentHashSet();
    private SLadderRank defaultRank;

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
        defaultRank = null;
        ladderRanks.clear();
        specialRanks.clear();

        try {
            OConfiguration ranksConfig = SuperiorPrisonPlugin.getInstance().getConfigController().getRanksConfig();
            String defaultPrefix = ranksConfig.getValueAsReq("default prefix");
            RequirementController rc = SuperiorPrisonPlugin.getInstance().getRequirementController();

            for (ConfigurationSection section : ranksConfig.getSections().values()) {
                // Is ladder rank
                if (section.isPresentValue("order")) {
                    Set<RequirementData> reqs = Sets.newHashSet();
                    section.ifValuePresent("requirements", List.class, list -> {
                        for (Object o : list) {
                            OPair<String, Optional<RequirementData>> data = rc.parse(o.toString());
                            if (data.getSecond().isPresent())
                                reqs.add(data.getSecond().get());

                            else
                                getPlugin().getOLogger().printWarning("Failed to find rankup requirement by id: " + data.getFirst() + " in " + section.getKey() + " rank!");
                        }
                    });

                    int order = section.getValueAsReq("order");
                    SLadderRank rank = new SLadderRank(
                            order,
                            section.getKey(),
                            section.hasValue("prefix") ? section.getValueAsReq("prefix", String.class) : defaultPrefix.replace("%rank_name%", section.getKey()),
                            section.hasValue("commands") ? (List<String>) section.getValueAsReq("commands", List.class) : new ArrayList<>(),
                            section.hasValue("permissions") ? (List<String>) section.getValueAsReq("permissions", List.class) : new ArrayList<>(),
                            reqs,
                            null,
                            null
                    );
                    if (ladderRanks.containsKey(order)) {
                        getPlugin().getOLogger().printWarning("Ladder rank with order " + order + " already exists, aborting rank creation!");
                        continue;
                    }
                    ladderRanks.put(order, rank);

                } else {
                    specialRanks.add(
                            new SSpecialRank(
                                    section.getKey(),
                                    section.hasValue("prefix") ? section.getValueAsReq("prefix", String.class) : defaultPrefix.replace("%rank_name%", section.getKey()),
                                    section.hasValue("commands") ? (List<String>) section.getValueAsReq("commands", List.class) : new ArrayList<>(),
                                    section.hasValue("permissions") ? (List<String>) section.getValueAsReq("permissions", List.class) : new ArrayList<>()
                            )
                    );
                }
            }

            ladderRanks.forEach((order, rank) -> {
                int previous = order - 1;
                int next = order + 1;

                Optional.ofNullable(ladderRanks.get(previous)).ifPresent(rank::setPreviousRank);
                Optional.ofNullable(ladderRanks.get(next)).ifPresent(rank::setNextRank);
            });

            defaultRank = ladderRanks.get(1);
            loaded = true;
        } catch (Throwable thrw) {
            throw new IllegalStateException("Failed to load RankController", thrw);
        }

        return true;
    }

    @Override
    public LadderRank getDefault() {
        return defaultRank;
    }

    @Override
    public Set<Rank> getRanks() {
        return new HashSet<Rank>() {{
            addAll(ladderRanks.values());
            addAll(specialRanks);
        }};
    }

    @Override
    public List<Rank> getSpecialRanks() {
        return new ArrayList<>(specialRanks);
    }

    @Override
    public List<LadderRank> getLadderRanks() {
        return new ArrayList<>(ladderRanks.values());
    }

    @Override
    public Optional<Rank> getRank(String name) {
        return getRanks()
                .stream()
                .filter(rank -> rank.getName().contentEquals(name))
                .findFirst();
    }

    @Override
    public Optional<LadderRank> getLadderRank(String name) {
        return getLadderRanks()
                .stream()
                .filter(rank -> rank.getName().contentEquals(name))
                .findFirst();
    }
}
