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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
            Player sender = command.getSenderAsPlayer();
            Optional<Boolean> all = command.getArg("all");

            if (prisoner.hasRank(rank.getName())) {
                LocaleEnum.PRISONER_ALREADY_HAVE_RANK.getWithErrorPrefix().send(sender, ImmutableMap.of("{prisoner}", prisoner.getOfflinePlayer().getName(), "{rank}", rank.getName()));
                return;
            }

            List<Rank> ranks = new ArrayList<>();
            ranks.add(rank);

            if (all.isPresent() && all.get() && rank instanceof SLadderRank)
                ranks.addAll(((SLadderRank) rank).getAllPrevious());

            prisoner.addRank(ranks.toArray(new Rank[0]));
            LocaleEnum.SUCCESSFULLY_ADDED_RANK.getWithPrefix().send(
                    sender,
                    ImmutableMap.of("{prisoner}", prisoner.getOfflinePlayer().getName(), "{rank}", ranks.size() == 1 ? rank.getName() : Arrays.toString(ranks.stream().map(Rank::getName).toArray()))
            );
        });
    }
}
