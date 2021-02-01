package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.event.mine.MineBlockBreakEvent;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.event.EventPriority;

public class StatisticsListener {
  public StatisticsListener() {
    DatabaseController databaseController =
        SuperiorPrisonPlugin.getInstance().getDatabaseController();
    SyncEvents.listen(
        MineBlockBreakEvent.class,
        EventPriority.LOWEST,
        event -> {
          if (event.isCancelled()) return;

          SStatisticsContainer statisticsContainer =
              databaseController.getStatisticHolder().getContainer(event.getPrisoner().getUUID());
          statisticsContainer
              .getBlocksStatistic()
              .update(OMaterial.matchMaterial(event.getBlock()), 1);
        });
  }
}
