package com.bgsoftware.superiorprison.plugin.commands.prisoner;

import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
import com.oop.orangeengine.command.OCommand;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdTeleport extends OCommand {

    public CmdTeleport() {
        label("teleport");
        alias("tp");
        description("teleport a prisoner into mine");

        argument(new PrisonerArg(false));
        argument(new MinesArg().setRequired(true));

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner", SPrisoner.class);
            SNormalMine superiorMine = command.getArgAsReq("mine");

            if (superiorMine.getPrisoners().contains(prisoner))
                return;

            int prisoners = superiorMine.getPlayerCount();
            if (superiorMine.getSettings().getPlayerLimit() != -1 && prisoners == superiorMine.getSettings().getPlayerLimit()) {
                messageBuilder(LocaleEnum.MINE_IS_FULL.getWithErrorPrefix())
                        .replace(superiorMine)
                        .send(command);
                return;
            }

            Framework.FRAMEWORK.teleport(prisoner.getPlayer(), superiorMine.getSpawnPoint());
        });
    }
}
