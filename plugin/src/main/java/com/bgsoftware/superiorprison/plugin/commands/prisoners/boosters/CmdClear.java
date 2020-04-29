package com.bgsoftware.superiorprison.plugin.commands.prisoners.boosters;

import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdClear extends OCommand {

    public CmdClear() {
        label("clear");
        argument(new PrisonerArg(true).setRequired(true));
        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            prisoner.getBoosters().clear();
            prisoner.save(true);

            messageBuilder(LocaleEnum.PRISONER_BOOSTER_CLEAR.getWithPrefix())
                    .replace(prisoner)
                    .send(command);
        });
    }

}
