package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
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
                .argument(
                        new StringArg()
                                .setIdentity("name")
                                .setRequired(true)
                ).listen(onCommand());
    }

    private Consumer<WrappedCommand> onCommand() {
        return command -> {
            Player player = (Player) command.getSender();
            String mineName = (String) command.getArg("name").get();
            Optional<SuperiorMine> mineOptional = SuperiorPrisonPlugin.getInstance().getMineController().getMine(mineName);

            if (!mineOptional.isPresent()) {
                //TODO: Configurable
                player.sendMessage(ChatColor.RED + "Invalid mine " + mineName + ".");
                return;
            }

            SuperiorMine superiorMine = mineOptional.get();

            if (!superiorMine.getSpawnPoint().isPresent()) {
                player.sendMessage(ChatColor.RED + "Mine " + mineName + " doesn't have a spawn point!");
                return;
            }

            player.teleport(superiorMine.getSpawnPoint().get().toBukkit());
            player.sendMessage(ChatColor.GREEN + "Teleported to mine " + mineName);
        };
    }

}
