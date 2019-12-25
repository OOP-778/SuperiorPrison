package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.google.common.collect.Sets;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.Location;
import org.bukkit.World;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CmdBenchmark extends OCommand {

    public CmdBenchmark() {
        label("benchmark");

        onCommand(command -> {
            command.getSender().sendMessage("Starting benchmark");

            Set<SNormalMine> createdNow = Sets.newConcurrentHashSet();

            for (int i = 0; i < 30; i++) {
                SNormalMine sNormalMine = new SNormalMine("bench-mine-" + i, randomLocation(command.getSenderAsPlayer().getWorld()), randomLocation(command.getSenderAsPlayer().getWorld()));
                createdNow.add(sNormalMine);

                SuperiorPrisonPlugin.getInstance().getMineController().getData().add(sNormalMine);
            }

            Instant then = Instant.now();
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
            final AtomicInteger counter = new AtomicInteger();
            final Collection<List<SNormalMine>> result = createdNow.stream()
                    .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / 3))
                    .values();

            for (List<SNormalMine> sNormalMines : result) {
                executorService.execute(() -> sNormalMines.forEach(mine -> SuperiorPrisonPlugin.getInstance().getDataController().save(mine)));
            }

            command.getSender().sendMessage("Took " + Duration.between(then, Instant.now()).toMillis() + "ms");
            createdNow.forEach(mine -> SuperiorPrisonPlugin.getInstance().getMineController().getData().remove(mine));
        });

    }

    private Location randomLocation(World world) {
        return new Location(world, ThreadLocalRandom.current().nextDouble(20), ThreadLocalRandom.current().nextDouble(40), ThreadLocalRandom.current().nextDouble(20));
    }

}
