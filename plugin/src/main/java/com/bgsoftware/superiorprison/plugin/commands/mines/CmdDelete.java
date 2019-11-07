package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.StringArg;

public class CmdDelete extends OCommand {

    public CmdDelete() {
        label("delete");
        argument(new MinesArg().setRequired(true));
        onCommand(command -> {
            SNormalMine mine = (SNormalMine) command.getArg("mine").get();

            command.getSender().sendMessage("Successfully deleted mine with name " + mine.getName());
            mine.preDelete();

            SuperiorPrisonPlugin.getInstance().getMineController().getData().remove(mine);
        });
    }
}
