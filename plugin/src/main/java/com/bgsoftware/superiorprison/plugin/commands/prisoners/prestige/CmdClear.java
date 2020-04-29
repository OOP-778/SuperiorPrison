package com.bgsoftware.superiorprison.plugin.commands.prisoners.prestige;

import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdClear extends OCommand {

    public CmdClear() {
        label("clear");
        description("Clear prestiges from prisoner");
        permission("superiorprison.admin");

        argument(new PrisonerArg(true).setRequired(true));

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            prisoner.clearPrestiges();

            messageBuilder(LocaleEnum.SUCCESSFULLY_CLEARED_PRESTIGES.getWithPrefix())
                    .replace(prisoner)
                    .send(command);
            prisoner.save(true);
        });
    }

}
