package com.bgsoftware.superiorprison.plugin.commands;

import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.IntArg;
import com.oop.orangeengine.main.task.OTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class CmdTestBackpacks extends OCommand {


    public CmdTestBackpacks() {
        label("testBackpacks");
        ableToExecute(Player.class);
        argument(new IntArg().setIdentity("seconds"));
        onCommand(cmd -> {
            Player player = cmd.getSenderAsPlayer();
            IntStream.rangeClosed(1, 5)
                    .forEach(i -> {
                        new OTask()
                                .delay(TimeUnit.SECONDS, (int) cmd.getArg("seconds").orElse(20))
                                .repeat(true)
                                .sync(false)
                                .consumer((it) -> {
                                    if (!player.isOnline()) {
                                        it.cancel();
                                        return;
                                    }
                                    player.getInventory().addItem(new ItemStack(Material.DIAMOND, ThreadLocalRandom.current().nextInt(5, 64)));
                                })
                                .execute();
                    });
        });
    }
}
