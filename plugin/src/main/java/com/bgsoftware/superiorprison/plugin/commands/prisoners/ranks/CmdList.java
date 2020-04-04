package com.bgsoftware.superiorprison.plugin.commands.prisoners.ranks;

import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.message.OMessage;

import java.util.List;
import java.util.stream.Collectors;

public class CmdList extends OCommand {
    public CmdList() {
        label("list");
        permission("superiorprison.admin");
        argument(new PrisonerArg(true).setRequired(true));
        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            OMessage withPrefix = LocaleEnum.PRISONER_RANKS_LIST.getWithPrefix();

            withPrefix.send(
                    command.getSenderAsPlayer(),
                    ImmutableMap.of(
                            "{prisoner}", prisoner.getOfflinePlayer().getName(),
                            "{ladder_ranks}", listToString(prisoner.getLadderRanks().stream().map(Rank::getName).collect(Collectors.toList())),
                            "{special_ranks}", listToString(prisoner.getSpecialRanks().stream().map(Rank::getName).collect(Collectors.toList()))
                    )
                    );
        });
    }

    public String listToString(List<String> list) {
        if (list.isEmpty()) return "None";
        return String.join(", ", list);
    }
}