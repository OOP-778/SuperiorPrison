package com.bgsoftware.superiorprison.plugin.commands.prisoners.ranks;

import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdList extends OCommand {
    public CmdList() {
        label("list");
        description("View prisoner ranks");
        permission("superiorprison.admin");
        argument(new PrisonerArg(true).setRequired(true));
        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            messageBuilder(LocaleEnum.PRISONER_RANKS_LIST.getWithPrefix())
                    .replace(prisoner)
                    .send(command);
        });
    }
}