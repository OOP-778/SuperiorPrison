package com.bgsoftware.superiorprison.plugin.commands.prisoner.rank;

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
        description("Reset ladder rank of the prisoner");
        argument(new PrisonerArg(true).setRequired(true));

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");

            ParsedObject parsed = Testing.ranksGenerator.getParsed(prisoner, 1).get();
            prisoner.setLadderRank(parsed.getIndex(), false);
            prisoner.save(true);

            messageBuilder(LocaleEnum.PRISONER_RANK_RESET.getWithPrefix())
                    .replace(prisoner)
                    .replace(parsed)
                    .send(command);
        });
    }
}
