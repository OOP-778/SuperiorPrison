package com.bgsoftware.superiorprison.plugin.commands.prisoner.boosters;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.IntArg;

import java.util.Optional;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdRemove extends OCommand {
    public CmdRemove() {
        label("remove");
        description("Remove a booster");
        argument(new PrisonerArg(true).setRequired(true));
        argument(new IntArg().setRequired(true).setIdentity("id").setDescription("An id of booster"));
        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            int id = command.getArgAsReq("id");

            Optional<Booster> booster = prisoner.getBoosters().findBoosterBy(id);
            if (!booster.isPresent()) {
                messageBuilder(LocaleEnum.PRISONER_BOOSTER_REMOVE_DOESNT_HAVE.getWithErrorPrefix())
                        .replace(prisoner)
                        .replace("{id}", id)
                        .send(command);
                return;
            }

            prisoner.getBoosters().removeBooster(booster.get());
            prisoner.save(true);

            messageBuilder(LocaleEnum.PRISONER_BOOSTER_REMOVE.getWithPrefix())
                    .replace(prisoner, booster.get())
                    .send(command);
        });
    }
}
