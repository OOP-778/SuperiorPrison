package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.api.data.player.LadderObject;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.main.task.OTask;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RankupTask extends OTask {
    private final Cache<UUID, Integer> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public RankupTask() {
        sync(false);
        delay(TimeUnit.SECONDS, SuperiorPrisonPlugin.getInstance().getMainConfig().getRankupMessageInterval());
        repeat(true);
        runnable(() -> {
            if (SuperiorPrisonPlugin.disabling) return;
            SuperiorPrisonPlugin.getInstance().getPrisonerController()
                    .streamOnline()
                    .filter(prisoner -> prisoner.getCurrentMine().isPresent())
                    .filter(prisoner -> cache.getIfPresent(prisoner.getUUID()) == null || cache.getIfPresent(prisoner.getUUID()) != prisoner.getLadderRank())
                    .filter(prisoner -> {
                        LadderObject rank = prisoner.getParsedLadderRank();
                        if (rank == null) return false;

                        Optional<LadderObject> next = rank.getNext();
                        if (!next.isPresent()) return false;

                        return ((ParsedObject) next.get()).getMeets().get();
                    })
                    .forEach(prisoner -> {
                        cache.invalidate(prisoner.getUUID());
                        cache.put(prisoner.getUUID(), prisoner.getLadderRank());
                        LocaleEnum.RANKUP_AVAILABLE.getWithPrefix().send(ImmutableMap.of("{rank_name}", prisoner.getParsedLadderRank().getNext().get().getName()), prisoner.getPlayer());
                    });
        });
        execute();
    }
}
