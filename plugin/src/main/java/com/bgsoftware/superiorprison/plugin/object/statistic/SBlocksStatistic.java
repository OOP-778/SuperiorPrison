package com.bgsoftware.superiorprison.plugin.object.statistic;

import com.bgsoftware.superiorprison.api.data.statistic.BlocksStatistic;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.material.OMaterial;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.bgsoftware.superiorprison.plugin.util.TimeUtil.getDate;

@EqualsAndHashCode
public class SBlocksStatistic implements BlocksStatistic, Attachable<SStatisticsContainer>, SStatistic {

    @Getter
    private transient SStatisticsContainer container;

    @Getter
    private transient long lastUpdated = 0;

    private Map<OMaterial, Long> minedBlocks = Maps.newConcurrentMap();

    private transient Cache<Long, OMaterial> timedCache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterAccess(SuperiorPrisonPlugin.getInstance().getMainConfig().getCacheTime(), TimeUnit.SECONDS)
            .build();

    @Override
    public void update(Material material, byte data, long amount) {
        update(OMaterial.matchMaterial(new ItemStack(material, 1, data)), amount);
    }

    public void update(OMaterial material, long amount) {
        minedBlocks.merge(material, amount, Long::sum);

        lastUpdated = getDate().toEpochSecond();
        timedCache.put(lastUpdated, material);
    }

    @Override
    public long getTotal() {
        return minedBlocks.values().stream().mapToLong(value -> value).sum();
    }

    @Override
    public long get(Material material, byte data) {
        return get(OMaterial.matchMaterial(new ItemStack(material, 1, data)));
    }

    public long get(OMaterial material) {
        return minedBlocks.getOrDefault(material, 0L);
    }

    @Override
    public void attach(SStatisticsContainer obj) {
        this.container = obj;
    }

    public long getTotalBlocksWithinTimeFrame(ZonedDateTime start, ZonedDateTime end) {
        long startLong = start.toEpochSecond();
        long endLong = end.toEpochSecond();
        return timedCache.asMap().keySet().stream().filter(timeLong -> startLong >= timeLong && timeLong <= endLong).count();
    }

    public long getBlockWithinTimeFrame(ZonedDateTime start, ZonedDateTime end, OMaterial material) {
        long startLong = start.toEpochSecond();
        long endLong = end.toEpochSecond();
        long[] sum = new long[]{0};

        timedCache.asMap().forEach((time, timedMaterial) -> {
            if (timedMaterial != material) return;
            if (!(startLong >= time && time <= endLong)) return;

            sum[0] = sum[0] + 1;
        });
        return sum[0];
    }
}
