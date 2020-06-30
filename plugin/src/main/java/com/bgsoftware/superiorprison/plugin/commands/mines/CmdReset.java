package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.task.StaticTask;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdReset extends OCommand {
    public CmdReset() {
        label("reset");
        description("Reset a mine");
        permission("superiorprison.reset");
        argument(new MinesArg().setRequired(true));
        onCommand(command -> {
            SNormalMine mine = command.getArgAsReq("mine");
            StaticTask.getInstance().async(() -> mine.getGenerator().reset());

            messageBuilder(LocaleEnum.MINE_RESET_SUCCESSFUL.getWithPrefix())
                    .replace(mine)
                    .send(command);
        });
    }
}
