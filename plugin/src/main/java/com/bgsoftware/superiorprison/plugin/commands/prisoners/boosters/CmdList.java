package com.bgsoftware.superiorprison.plugin.commands.prisoners.boosters;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.listedBuilder;

public class CmdList extends OCommand {
    public CmdList() {
        label("list");
        description("Show a list of boosters");
        argument(new PrisonerArg(true).setRequired(true));
        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");

            listedBuilder(Booster.class)
                    .message(LocaleEnum.PRISONER_BOOSTER_LIST.getWithPrefix())
                    .identifier("{TEMPLATE}")
                    .addObject(prisoner.getBoosters().set().toArray(new Booster[0]))
                    .addPlaceholderObject(prisoner)
                    .send(command);
        });
    }
}
