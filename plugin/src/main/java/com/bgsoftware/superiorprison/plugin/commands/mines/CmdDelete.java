package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdDelete extends OCommand {
    public CmdDelete() {
        label("delete");
        argument(new MinesArg().setRequired(true));
        onCommand(command -> {
            SNormalMine mine = (SNormalMine) command.getArg("mine").get();
            mine.remove(true);

            messageBuilder(LocaleEnum.MINE_DELETE_SUCCESSFUL.getWithPrefix())
                    .replace(mine)
                    .send(command);
        });
    }
}
