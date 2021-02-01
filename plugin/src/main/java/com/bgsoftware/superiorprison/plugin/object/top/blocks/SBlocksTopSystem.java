package com.bgsoftware.superiorprison.plugin.object.top.blocks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.statistic.SBlocksStatistic;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer;
import com.bgsoftware.superiorprison.plugin.object.top.STopSystem;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SBlocksTopSystem extends STopSystem<SBlocksStatistic, BlockTopEntry> {
  @Override
  public String getName() {
    return "Blocks";
  }

  @Override
  protected Comparator<SBlocksStatistic> comparator() {
    return Comparator.comparingLong(SBlocksStatistic::getTotal);
  }

  @Override
  protected Stream<SBlocksStatistic> stream() {
    return SuperiorPrisonPlugin.getInstance().getStatisticsController().stream()
        .filter(container -> container.getPrisoner() != null)
        .map(SStatisticsContainer::getBlocksStatistic);
  }

  @Override
  protected Predicate<SBlocksStatistic> filter() {
    return b -> true;
  }

  @Override
  protected BiFunction<SBlocksStatistic, Integer, BlockTopEntry> constructor() {
    return (s, p) -> new BlockTopEntry(s.getContainer().getPrisoner(), s, p);
  }
}
