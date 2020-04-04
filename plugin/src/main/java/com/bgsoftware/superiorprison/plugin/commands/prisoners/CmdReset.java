package com.bgsoftware.superiorprison.plugin.commands.prisoners;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.command.OCommand;
import jdk.nashorn.internal.ir.annotations.Immutable;

public class CmdReset extends OCommand {
    public CmdReset() {
        label("reset");
        description("Reset prisoners data");
        permission("superiorprison.admin");
        argument(new PrisonerArg(true).setRequired(true));

        onCommand(command -> {
            Prisoner prisoner = command.getArgAsReq("prisoner");
            ((SPrisoner)prisoner).remove(true);

            LocaleEnum.SUCCESSFULLY_RESET_PRISONER.getWithPrefix().send(command.getSenderAsPlayer(), ImmutableMap.of("{prisoner}", prisoner.getOfflinePlayer().getName()));
        });
    }
}
