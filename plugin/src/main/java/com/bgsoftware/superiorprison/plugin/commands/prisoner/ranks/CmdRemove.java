package com.bgsoftware.superiorprison.plugin.commands.prisoner.ranks;

import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.commands.args.RanksArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdRemove extends OCommand {
    public CmdRemove() {
        label("remove");
        description("Remove a rank to prisoner");

        argument(new PrisonerArg(true).setRequired(true));
        argument(new RanksArg(false, false).setRequired(true));

        onCommand(
                command -> {
                    SPrisoner prisoner = command.getArgAsReq("prisoner");
                    Rank rank = command.getArgAsReq("rank");

                    prisoner.removeRank(rank);
                    messageBuilder(LocaleEnum.SUCCESSFULLY_REMOVED_RANK.getWithPrefix())
                            .replace("{rank_name}", rank.getName())
                            .replace(prisoner)
                            .send(command);
                });
    }
}
