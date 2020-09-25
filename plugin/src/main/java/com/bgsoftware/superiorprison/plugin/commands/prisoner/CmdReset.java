package com.bgsoftware.superiorprison.plugin.commands.prisoner;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.Helper;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdReset extends OCommand {
    public CmdReset() {
        label("reset");
        description("Reset prisoners data");
        permission("superiorprison.admin");
        argument(new PrisonerArg(true).setRequired(true));

        onCommand(command -> {
            Prisoner prisoner = command.getArgAsReq("prisoner");
            prisoner.remove();

            if (prisoner.isOnline())
                prisoner.getPlayer().kickPlayer(Helper.color(LocaleEnum.PRISONER_RESET.getWithPrefix().raw()[0]));

            messageBuilder(LocaleEnum.SUCCESSFULLY_RESET_PRISONER.getWithPrefix())
                    .replace(prisoner)
                    .send(command);
        });
    }
}
