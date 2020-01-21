package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.WrappedCommand;
import com.oop.orangeengine.command.arg.arguments.StringArg;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.function.Consumer;

public class CmdTeleport extends OCommand {

    public CmdTeleport() {
        label("teleport")
                .alias("tp", "visit")
                .ableToExecute(Player.class)
                .argument(new MinesArg().setRequired(true))
                .listen(onCommand());
    }

    private Consumer<WrappedCommand> onCommand() {
        return command -> {
            Player player = (Player) command.getSender();
            SNormalMine superiorMine = command.getArgAsReq("mine");

            if (!superiorMine.getSpawnPoint().isPresent()) {
                player.sendMessage(ChatColor.RED + "Mine " + superiorMine.getName() + " doesn't have a spawn point!");
                return;
            }

            player.teleport(superiorMine.getSpawnPoint().get().toBukkit());
            player.sendMessage(ChatColor.GREEN + "Teleported to mine " + superiorMine.getName());
        };
    }

}
