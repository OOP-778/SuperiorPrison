package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;

public class CmdDelete extends OCommand {

    public CmdDelete() {
        label("delete");
        argument(new MinesArg().setRequired(true));
        onCommand(command -> {
            SNormalMine mine = (SNormalMine) command.getArg("mine").get();
            mine.remove(true);

            command.getSender().sendMessage("Successfully deleted mine with name " + mine.getName());
        });
    }
}
