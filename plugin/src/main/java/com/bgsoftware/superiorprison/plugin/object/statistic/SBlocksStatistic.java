package com.bgsoftware.superiorprison.plugin.object.statistic;

import static com.bgsoftware.superiorprison.plugin.util.TimeUtil.getDate;

import com.bgsoftware.superiorprison.api.data.statistic.BlocksStatistic;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@EqualsAndHashCode
public class SBlocksStatistic
    implements BlocksStatistic, Attachable<SStatisticsContainer>, SStatistic, SerializableObject {

  private final Map<OMaterial, Long> minedBlocks = Maps.newConcurrentMap();
  private final transient Cache<Long, OPair<OMaterial, Long>> timedCache =
      CacheBuilder.newBuilder()
          .concurrencyLevel(4)
          .expireAfterAccess(
              SuperiorPrisonPlugin.getInstance().getMainConfig().getCacheTime(), TimeUnit.SECONDS)
          .build();
  private final AtomicLong totalBlocks = new AtomicLong();
  @Getter private transient SStatisticsContainer container;
  @Getter private transient long lastUpdated = 0;

  @Override
  public void update(Material material, byte data, long amount) {
    update(OMaterial.matchMaterial(new ItemStack(material, 1, data)), amount);
  }

  @SneakyThrows
  public void update(OMaterial material, long amount) {
    totalBlocks.addAndGet(amount);
    minedBlocks.merge(material, amount, Long::sum);

    lastUpdated = getDate().toEpochSecond();
    OPair<OMaterial, Long> data = timedCache.get(lastUpdated, () -> new OPair<>(material, 0L));
    data.setSecond(data.getSecond() + amount);
  }

  @Override
  public long getTotal() {
    return totalBlocks.get();
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

    return timedCache.asMap().keySet().stream()
        .filter(timeLong -> timeLong >= startLong && timeLong <= endLong)
        .map(timedCache::getIfPresent)
        .filter(Objects::nonNull)
        .mapToLong(OPair::getSecond)
        .sum();
  }

  public long getBlockWithinTimeFrame(ZonedDateTime start, ZonedDateTime end, OMaterial material) {
    long startLong = start.toEpochSecond();
    long endLong = end.toEpochSecond();
    long[] sum = new long[] {0};

    timedCache
        .asMap()
        .forEach(
            (time, timedMaterial) -> {
              if (timedMaterial.getFirst() != material) return;
              if (!(startLong >= time && time <= endLong)) return;

              sum[0] = sum[0] + timedMaterial.getSecond();
            });
    return sum[0];
  }

  @Override
  public void serialize(SerializedData serializedData) {
    serializedData.write("blocks", minedBlocks);
  }

  @Override
  public void deserialize(SerializedData serializedData) {
    if (serializedData.getElement("blocks").isPresent()) {
      serializedData
          .applyAsMap("blocks")
          .forEach(
              pair -> {
                OMaterial key = pair.getKey().applyAs(OMaterial.class);
                long value = pair.getValue().applyAs(long.class);
                minedBlocks.put(key, value);
              });
    }
    totalBlocks.set(minedBlocks.values().stream().mapToLong(value -> value).sum());
  }
}
