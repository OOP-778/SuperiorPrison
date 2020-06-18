package com.bgsoftware.superiorprison.plugin.commands.prisoner.prestige;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.plugin.commands.args.PrestigesArg;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
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
        description("Add a prestige to prisoner");
        permission("superiorprison.admin");

        argument(new PrisonerArg(true).setRequired(true));
        argument(new PrestigesArg().setRequired(true));
        argument(new BoolArg().setIdentity("all").setDescription("true or false, if true when added prestige, it will add all the previous prestiges"));

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            Prestige prestige = command.getArgAsReq("prestige");
            Optional<Boolean> all = command.getArg("all");

            if (prisoner.hasPrestige(prestige.getName())) {
                messageBuilder(LocaleEnum.PRISONER_ALREADY_HAVE_PRESTIGE.getWithErrorPrefix())
                        .replace(prestige, prisoner)
                        .send(command);
                return;
            }

            List<Prestige> prestiges = new ArrayList<>();
            prestiges.add(prestige);

            if (all.isPresent())
                prestiges.addAll(prestige.getAllPrevious());

            prisoner.addPrestige(prestiges.toArray(new Prestige[0]));
            messageBuilder(LocaleEnum.SUCCESSFULLY_ADDED_PRESTIGE.getWithPrefix())
                    .replace("{prestige_name}", prestiges.size() == 1 ? prestige.getName() : Arrays.toString(prestiges.stream().map(Prestige::getName).toArray()))
                    .replace(prisoner, prestige)
                    .send(command);
            prisoner.save(true);
        });
    }
}
