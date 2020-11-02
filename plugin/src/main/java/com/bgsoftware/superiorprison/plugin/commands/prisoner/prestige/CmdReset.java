package com.bgsoftware.superiorprison.plugin.commands.prisoner.prestige;

import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.Testing;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.oop.orangeengine.command.OCommand;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdReset extends OCommand {
    public CmdReset() {
        label("reset");
        description("Reset prestige of the prisoner");
        argument(new PrisonerArg(true).setRequired(true));

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");

            prisoner._setPrestige(0);
            prisoner.save(true);

            messageBuilder(LocaleEnum.PRISONER_PRESTIGE_RESET.getWithPrefix())
                    .replace(prisoner)
                    .send(command);
        });
    }
}
