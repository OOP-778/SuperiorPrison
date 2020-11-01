package com.bgsoftware.superiorprison.plugin.commands.prisoner.rank;

import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.commands.args.RanksArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.oop.orangeengine.command.OCommand;

import java.util.function.Function;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdSet extends OCommand {
    public CmdSet() {
        label("setladder");
        description("Set ladder rank of the prisoner");
        argument(new PrisonerArg(true).setRequired(true));
        argument(new RanksArg().setRequired(true));

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            Function<SPrisoner, ParsedObject> ladderRank = command.getArgAsReq("rank");

            prisoner.setLadderRank(ladderRank.apply(prisoner).getIndex(), true);
            prisoner.save(true);

            messageBuilder(LocaleEnum.PRISONER_RANK_SET.getWithPrefix())
                    .replace(prisoner)
                    .replace(ladderRank.apply(prisoner))
                    .send(command);
        });
    }
}
