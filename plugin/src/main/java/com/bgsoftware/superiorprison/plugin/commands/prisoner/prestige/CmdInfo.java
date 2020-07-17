package com.bgsoftware.superiorprison.plugin.commands.prisoner.prestige;

import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdInfo extends OCommand {
    public CmdInfo() {
        label("info");
        description("Get information about prisoner prestige");
        argument(new PrisonerArg(true).setRequired(true));
        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            messageBuilder(LocaleEnum.PRISONER_PRESTIGE_VIEW.getWithPrefix())
                    .replace(prisoner)
                    .send(command);
        });
    }
}
