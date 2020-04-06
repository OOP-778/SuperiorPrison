package com.bgsoftware.superiorprison.plugin.commands.prisoners.prestige;

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

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdClear extends OCommand {

    public CmdClear() {
        label("clear");
        description("Clear prestiges from prisoner");
        permission("superiorprison.admin");

        argument(new PrisonerArg(true).setRequired(true));

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            prisoner.clearPrestiges();

            messageBuilder(LocaleEnum.SUCCESSFULLY_CLEARED_PRESTIGES.getWithPrefix())
                    .replace(prisoner)
                    .send(command);
            prisoner.save(true);
        });
    }

}
