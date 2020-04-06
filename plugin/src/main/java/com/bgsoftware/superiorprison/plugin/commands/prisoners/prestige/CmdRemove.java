package com.bgsoftware.superiorprison.plugin.commands.prisoners.prestige;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.plugin.commands.args.PrestigesArg;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.commands.args.RanksArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrestige;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.BoolArg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdRemove extends OCommand {
    public CmdRemove() {
        label("remove");
        description("Remove a prestige from prisoner");
        argument(new PrisonerArg(true).setRequired(true));
        argument(new PrestigesArg().setRequired(true));
        argument(new BoolArg().setIdentity("all").setDescription("true or false, if true when removed prestige, it will remove all the previous prestiges"));

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            SPrestige prestige = command.getArgAsReq("prestige");
            Optional<Boolean> all = command.getArg("all");

            List<Prestige> prestiges = new ArrayList<>();
            prestiges.add(prestige);

            if (all.isPresent())
                prestiges.addAll(prestige.getAllPrevious());

            prisoner.removePrestige((String[]) prestiges.stream().map(Prestige::getName).toArray(String[]::new));
            messageBuilder(LocaleEnum.SUCCESSFULLY_REMOVED_PRESTIGE.getWithPrefix())
                    .replace("{prestige_name}", prestiges.size() == 1 ? prestige.getName() : Arrays.toString(prestiges.stream().map(Prestige::getName).toArray()))
                    .replace(prisoner, prestige);

            prisoner.save(true);
        });
    }
}
