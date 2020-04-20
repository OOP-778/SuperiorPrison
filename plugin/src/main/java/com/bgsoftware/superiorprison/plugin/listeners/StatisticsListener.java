package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import static com.bgsoftware.superiorprison.plugin.util.TimeUtil.getDate;

public class StatisticsListener {
    public StatisticsListener() {
        DatabaseController databaseController = SuperiorPrisonPlugin.getInstance().getDatabaseController();
        SyncEvents.listen(BlockBreakEvent.class, EventPriority.MONITOR, event -> {
            System.out.println(event.isCancelled());
            if (event.isCancelled()) return;

            SPrisoner prisoner = databaseController.getPrisonerHolder().getInsertIfAbsent(event.getPlayer());
            if (!prisoner.getCurrentMine().isPresent()) return;

            SStatisticsContainer statisticsContainer = databaseController.getStatisticHolder().getContainer(prisoner.getUUID());
            statisticsContainer.getBlocksStatistic().update(OMaterial.matchMaterial(event.getBlock()), 1);

            long totalBlocksWithinTimeFrame = statisticsContainer.getBlocksStatistic().getTotalBlocksWithinTimeFrame(getDate().minusSeconds(10), getDate());
            System.out.println("You have mined " + totalBlocksWithinTimeFrame + " for the 10 seconds!");
        });
    }
}
