package com.bgsoftware.superiorprison.plugin.commands.prisoner.ranks;

import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.commands.args.RanksArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.BoolArg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdAdd extends OCommand {
    public CmdAdd() {
        label("add");
        description("Add a rank to prisoner");
        permission("superiorprison.admin");

        argument(new PrisonerArg(true).setRequired(true));
        argument(new RanksArg(false, true).setRequired(true));

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            Rank rank = command.getArgAsReq("rank");

            if (rank instanceof LadderRank) {
                LocaleEnum.PRISONER_RANKS_ADD_CANNOT_LADDER
                        .getWithErrorPrefix()
                        .send(command.getSender());
                return;
            }

            if (prisoner.hasRank(rank)) {
                messageBuilder(LocaleEnum.PRISONER_ALREADY_HAVE_RANK.getWithErrorPrefix())
                        .replace(rank, prisoner)
                        .send(command);
                return;
            }

            prisoner.addRank(rank);
            messageBuilder(LocaleEnum.SUCCESSFULLY_ADDED_RANK.getWithPrefix())
                    .replace("{rank_name}", rank.getName())
                    .replace(prisoner, rank)
                    .send(command);
        });
    }
}
