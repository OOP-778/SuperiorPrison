package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.main.task.OTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RankupTask extends OTask {
    private final Cache<UUID, String> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public RankupTask() {
        sync(false);
        delay(TimeUnit.SECONDS, SuperiorPrisonPlugin.getInstance().getMainConfig().getRankupMessageInterval());
        repeat(true);
        runnable(() -> {
            if (SuperiorPrisonPlugin.disabling) return;
            if (SuperiorPrisonPlugin.getInstance().getMainConfig().isDisableRankupMessage()) return;

            SuperiorPrisonPlugin.getInstance().getPrisonerController()
                    .streamOnline()
                    .filter(prisoner -> prisoner.getCurrentMine().isPresent())
                    .filter(prisoner -> {
                        String ifPresent = cache.getIfPresent(prisoner.getUUID());
                        if (ifPresent == null) return true;

                        return !ifPresent.contentEquals(prisoner.getCurrentLadderRank().getName());
                    })
                    .filter(prisoner -> {
                        LadderRank rank = prisoner.getCurrentLadderRank();
                        if (rank == null) return false;

                        Optional<SLadderRank> next = rank.getNext().map(r -> (SLadderRank) r);
                        if (!next.isPresent()) return false;

                        List<RequirementException> failed = new ArrayList<>();
                        next.get().getRequirements()
                                .forEach(data -> {
                                    Optional<Requirement> requirement = SuperiorPrisonPlugin.getInstance().getRequirementController().findRequirement(data.getType());
                                    try {
                                        requirement.get().getHandler().testIO(prisoner, data);
                                    } catch (RequirementException ex) {
                                        failed.add(ex);
                                    }
                                });
                        return failed.isEmpty();
                    })
                    .forEach(prisoner -> {
                        cache.invalidate(prisoner.getUUID());
                        cache.put(prisoner.getUUID(), prisoner.getCurrentLadderRank().getName());
                        LocaleEnum.RANKUP_AVAILABLE.getWithPrefix().send(ImmutableMap.of("{rank_name}", prisoner.getCurrentLadderRank().getNext().get().getName()), prisoner.getPlayer());
                    });
        });
        execute();
    }
}
