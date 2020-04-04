package com.bgsoftware.superiorprison.plugin.commands.prisoners.ranks;

import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.plugin.commands.args.ClearTypeArg;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SSpecialRank;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.function.Predicate;

public class CmdClear extends OCommand {

    public CmdClear() {
        label("clear");
        description("Clear ranks from prisoner");
        permission("superiorprison.admin");

        argument(new PrisonerArg(true).setRequired(true));
        argument(new ClearTypeArg());

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            Player sender = command.getSenderAsPlayer();
            Optional<String> clearType = command.getArg("type");

            LocaleEnum.SUCCESSFULLY_CLEARED_RANKS.getWithPrefix().send(sender, ImmutableMap.of("{prisoner}", prisoner.getOfflinePlayer().getName()));
            if (clearType.isPresent()) {
                Predicate<Rank> filter;
                if (clearType.get().equalsIgnoreCase("ladder"))
                    filter = rank -> rank instanceof SLadderRank;

                else
                    filter = rank -> rank instanceof SSpecialRank;

                prisoner.removeRankIf(filter);
            } else
                prisoner.clearRanks();
        });
    }

}
