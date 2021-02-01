package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.reward.MineReward;
import com.bgsoftware.superiorprison.api.event.mine.MultiBlockBreakEvent;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.task.StaticTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import org.bukkit.Bukkit;

public class RewardsListener {
  public RewardsListener() {
    Supplier<Double> gen = () -> ThreadLocalRandom.current().nextDouble();
    SyncEvents.listen(
        MultiBlockBreakEvent.class,
        event -> {
          StaticTask.getInstance()
              .ensureSync(
                  () -> {
                    event
                        .getBlockData()
                        .forEach(
                            (k, v) -> {
                              for (MineReward reward : event.getMine().getRewards().getRewards()) {
                                if (gen.get() <= reward.getChance()) {
                                  for (String command : reward.getCommands())
                                    Bukkit.dispatchCommand(
                                        Bukkit.getConsoleSender(),
                                        command.replace(
                                            "%player%", event.getPrisoner().getPlayer().getName()));
                                  break;
                                }
                              }
                            });
                  });
        });
  }
}
