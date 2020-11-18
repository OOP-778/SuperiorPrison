package com.bgsoftware.superiorprison.plugin.commands.prisoner.prestige;

import com.bgsoftware.superiorprison.plugin.commands.args.PrestigesArg;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.oop.orangeengine.command.OCommand;

import java.util.function.Function;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdSet extends OCommand {
    public CmdSet() {
        label("set");
        description("Set prisoners prestige");
        argument(new PrisonerArg(true).setRequired(true));
        argument(new PrestigesArg().setRequired(true));
        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            Function<SPrisoner, ParsedObject> prestige = command.getArgAsReq("prestige");

            prisoner.setPrestige(prestige.apply(prisoner).getIndex(), true);
            prisoner.save(true);

            messageBuilder(LocaleEnum.PRISONER_PRESTIGE_SET.getWithPrefix())
                    .replace(prisoner)
                    .replace(prestige.apply(prisoner))
                    .send(command);
        });
    }
}
