package com.bgsoftware.superiorprison.plugin.commands.prisoners.ranks;

import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.commands.args.RanksArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.BoolArg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CmdRemove extends OCommand {
    public CmdRemove() {
        label("remove");
        description("Remove a rank from prisoner");
        argument(new PrisonerArg(true).setRequired(true));
        argument(new RanksArg().setRequired(true));
        argument(new BoolArg().setIdentity("all").setDescription("true or false, if true when removed ladder rank, it will remove all the previous ranks"));

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            Rank rank = command.getArgAsReq("rank");
            Optional<Boolean> all = command.getArg("all");

            List<Rank> ranks = new ArrayList<>();
            ranks.add(rank);

            if (all.isPresent() && all.get() && rank instanceof SLadderRank)
                ranks.addAll(((SLadderRank) rank).getAllPrevious());

            prisoner.removeRank((String[]) ranks.stream().map(Rank::getName).toArray(String[]::new));
            LocaleEnum.SUCCESSFULLY_REMOVED_RANK.getWithPrefix().send(
                    command.getSenderAsPlayer(),
                    ImmutableMap.of("{prisoner}", prisoner.getOfflinePlayer().getName(), "{rank}", ranks.size() == 1 ? rank.getName() : Arrays.toString(ranks.stream().map(Rank::getName).toArray()))
            );
        });
    }
}
