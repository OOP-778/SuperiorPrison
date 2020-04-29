package com.bgsoftware.superiorprison.plugin.commands.prisoners.ranks;

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
        argument(new RanksArg().setRequired(true));
        argument(new BoolArg().setIdentity("all").setDescription("true or false, if true when added ladder rank, it will add all the previous ranks"));

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            Rank rank = command.getArgAsReq("rank");
            Optional<Boolean> all = command.getArg("all");

            if (prisoner.hasPrestige(rank.getName())) {
                messageBuilder(LocaleEnum.PRISONER_ALREADY_HAVE_RANK.getWithErrorPrefix())
                        .replace(rank, prisoner)
                        .send(command);
                return;
            }

            List<Rank> ranks = new ArrayList<>();
            ranks.add(rank);

            if (all.isPresent() && rank instanceof LadderRank)
                ranks.addAll(((LadderRank) rank).getAllPrevious());

            prisoner.addRank(ranks.toArray(new Rank[0]));
            messageBuilder(LocaleEnum.SUCCESSFULLY_ADDED_RANK.getWithPrefix())
                    .replace("{rank_name}", ranks.size() == 1 ? rank.getName() : Arrays.toString(ranks.stream().map(Rank::getName).toArray()))
                    .replace(prisoner, rank)
                    .send(command);
        });
    }
}
