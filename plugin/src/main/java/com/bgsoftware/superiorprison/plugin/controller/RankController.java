package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SSpecialRank;
import com.bgsoftware.superiorprison.plugin.requirement.LoadingRequirementData;
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

public class RankController implements com.bgsoftware.superiorprison.api.controller.RankController, OComponent<SuperiorPrisonPlugin> {

    private final Map<Integer, SLadderRank> ladderRanks = Maps.newConcurrentMap();
    private final Set<SSpecialRank> specialRanks = Sets.newConcurrentHashSet();
    private SLadderRank defaultRank;

    @Override
    public boolean load() {
        defaultRank = null;
        ladderRanks.clear();
        specialRanks.clear();

        try {
            Config ranksConfig = SuperiorPrisonPlugin.getInstance().getConfigController().getRanksConfig();
            String defaultPrefix = ranksConfig.getAs("default prefix");
            RequirementController rc = SuperiorPrisonPlugin.getInstance().getRequirementController();

            for (ConfigSection section : ranksConfig.getSections().values()) {
                // Is ladder rank
                if (section.isValuePresent("order")) {
                    Set<RequirementData> reqs = Sets.newHashSet();
                    section.ifValuePresent("requirements", List.class, list -> {
                        for (Object o : list) {
                            if (o.toString().trim().length() == 0) continue;

                            OPair<String, RequirementData> data = rc.parse(o.toString());
                            reqs.add(data.getSecond());

                            if (data.getSecond() instanceof LoadingRequirementData) {
                                getPlugin().getOLogger().printWarning("Requirement by id {} is not found, converting the requirement to loading requirement...", data.getSecond().getType());
                                ((LoadingRequirementData) data.getSecond()).setOnLoad(reqData -> {
                                    reqs.remove(data.getSecond());
                                    reqs.add(reqData);
                                });
                            }
                        }
                    });

                    int order = section.getAs("order");
                    SLadderRank rank = new SLadderRank(
                            order,
                            section.getKey(),
                            section.isValuePresent("prefix") ? section.getAs("prefix", String.class) : defaultPrefix.replace("{rank_name}", section.getKey()),
                            section.isValuePresent("commands") ? (List<String>) section.getAs("commands", List.class) : new ArrayList<>(),
                            section.isValuePresent("permissions") ? (List<String>) section.getAs("permissions", List.class) : new ArrayList<>(),
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
                                    section.isValuePresent("prefix") ? section.getAs("prefix", String.class) : defaultPrefix.replace("%rank_name%", section.getKey()),
                                    section.isValuePresent("commands") ? (List<String>) section.getAs("commands", List.class) : new ArrayList<>(),
                                    section.isValuePresent("permissions") ? (List<String>) section.getAs("permissions", List.class) : new ArrayList<>()
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
        } catch (Throwable thrw) {
            thrw.printStackTrace();
            throw new IllegalStateException("Failed to load RankController cause " + thrw.getMessage(), thrw);
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
