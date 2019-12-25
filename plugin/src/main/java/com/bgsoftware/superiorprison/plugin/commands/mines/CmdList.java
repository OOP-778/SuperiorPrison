package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

public class CmdList extends OCommand {

    public CmdList() {
        label("list");
        alias("ls");
        description("Lists all available mines");
        ableToExecute(Player.class);
        onCommand(command -> {
            for (SuperiorMine mine : SuperiorPrisonPlugin.getInstance().getMineController().getMines()) {

                command.getSender().sendMessage("Mine - " + mine.getName());

            }
        });
    }

}
