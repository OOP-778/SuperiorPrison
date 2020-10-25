package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.reward.MineReward;
import com.bgsoftware.superiorprison.api.event.mine.MultiBlockBreakEvent;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.Bukkit;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class RewardsListener {
    public RewardsListener() {
        Supplier<Double> gen = () -> ThreadLocalRandom.current().nextDouble();
        SyncEvents.listen(MultiBlockBreakEvent.class, event -> {
            event.getBlockData().forEach((k, v) -> {
                for (MineReward reward : event.getMine().getRewards().getRewards()) {
                    if (gen.get() <= reward.getChance()) {
                        for (String command : reward.getCommands())
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", event.getPrisoner().getPlayer().getName()));
                        break;
                    }
                }
            });
        });
    }
}
