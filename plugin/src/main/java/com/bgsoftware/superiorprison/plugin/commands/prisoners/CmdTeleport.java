package com.bgsoftware.superiorprison.plugin.commands.prisoners;

import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdTeleport extends OCommand {

    public CmdTeleport() {
        label("teleport");
        alias("tp");
        description("teleport prisoner into a mine");

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

            prisoner.getPlayer().teleport(superiorMine.getSpawnPoint());
        });
    }
}
