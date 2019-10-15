package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.api.data.mine.ISuperiorMine;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.WrappedCommand;
import com.oop.orangeengine.command.arg.arguments.StringArg;
import com.oop.orangeengine.main.util.OptionalConsumer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class CmdInfo extends OCommand {

    public CmdInfo(){
        label("info")
        .argument(
                new StringArg()
                .setIdentity("name")
                .setIsRequired(true)
        ).listen(onCommand());
    }

    private Consumer<WrappedCommand> onCommand(){
        return command -> {
            Player player = (Player) command.getSender();
            String mineName = (String) command.getArg("name").get();

            //TODO: OOP, create that freaking database
            OptionalConsumer<ISuperiorMine> mineOptional = /*SuperiorPrisonPlugin.getInstance().getMineController().getMineByName(mineName)*/ null;

            if(!mineOptional.isPresent()){
                //TODO: Configurable
                player.sendMessage(ChatColor.RED + "Invalid mine " + mineName + ".");
                return;
            }

            ISuperiorMine superiorMine = mineOptional.get();

            player.sendMessage(ChatColor.YELLOW + "Mine Information:\n" +
                    "Name: " + superiorMine.getName() + "\n" +
                    "Spawn Point: " + superiorMine.getSpawnPoint() + "\n" +
                    "Minimum: " + superiorMine.getMinPoint() + "\n" +
                    "Maximum: " + superiorMine.getHighPoint()
            );
        };
    }

}
