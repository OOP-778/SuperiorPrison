package com.bgsoftware.superiorprison.plugin.commands.prisoners;

import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CmdTeleport extends OCommand {

    public CmdTeleport() {
        label("teleport");
        alias("tp");
        description("teleport prisoner into a mine");
        ableToExecute(Player.class);

        argument(new PrisonerArg(false));
        argument(new MinesArg().setRequired(true));

        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner", SPrisoner.class);
            SNormalMine superiorMine = command.getArgAsReq("mine");

            if (superiorMine.getPrisoners().contains(prisoner))
                return;

            prisoner.getPlayer().teleport(superiorMine.getSpawnPoint().get().toBukkit());
        });
    }
}
