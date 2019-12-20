package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.Location;
import org.bukkit.World;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CmdBenchmark extends OCommand {

    public CmdBenchmark() {
        label("benchmark");

        onCommand(command -> {
            command.getSender().sendMessage("Starting benchmark");

            List<SNormalMine> createdNow = new ArrayList<>();

            for (int i = 0; i < 60; i++) {
                SNormalMine sNormalMine = new SNormalMine("bench-mine-" + i, randomLocation(command.getSenderAsPlayer().getWorld()), randomLocation(command.getSenderAsPlayer().getWorld()));
                createdNow.add(sNormalMine);

                SuperiorPrisonPlugin.getInstance().getMineController().getData().add(sNormalMine);
            }

            Instant then = Instant.now();
            for (SNormalMine mine : createdNow)
                SuperiorPrisonPlugin.getInstance().getDataController().save(mine);

            command.getSender().sendMessage("Took " + Duration.between(then, Instant.now()).toMillis() + "ms");
            createdNow.forEach(mine ->  SuperiorPrisonPlugin.getInstance().getMineController().getData().remove(mine));
        });

    }

    private Location randomLocation(World world) {
        return new Location(world, ThreadLocalRandom.current().nextDouble(20), ThreadLocalRandom.current().nextDouble(40), ThreadLocalRandom.current().nextDouble(20));
    }

}
