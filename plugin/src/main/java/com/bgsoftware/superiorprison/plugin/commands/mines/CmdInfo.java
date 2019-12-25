package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.WrappedCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class CmdInfo extends OCommand {

    public CmdInfo() {
        label("info")
                .argument(
                        new MinesArg().setRequired(true)
                ).listen(onCommand());
    }

    private Consumer<WrappedCommand> onCommand() {
        return command -> {
            Player player = (Player) command.getSender();
            SuperiorMine superiorMine = command.getArgAsReq("mine");

            player.sendMessage(ChatColor.YELLOW + "Mine Information:\n" +
                    "Name: " + superiorMine.getName() + "\n" +
                    "Spawn Point: " + superiorMine.getSpawnPoint().toString() + "\n" +
                    "Minimum: " + superiorMine.getMinPoint() + "\n" +
                    "Maximum: " + superiorMine.getHighPoint() + "\n" +
                    "Radius: " + calculateRadius(superiorMine.getMinPoint(), superiorMine.getHighPoint())
            );
        };
    }

    private int calculateRadius(SPLocation minPoint, SPLocation highPoint) {
        double diffX = Math.pow(2, (minPoint.x() - highPoint.x()));
        double diffZ = Math.pow(2, (minPoint.z() - highPoint.z()));
        return (int) Math.round(Math.sqrt(diffX + diffZ));
    }
}
