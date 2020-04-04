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
        ableToExecute(Player.class);

        argument(new PrisonerArg(false));
        argument(new MinesArg().setRequired(true));

        onCommand(command -> {
            Player player = command.getArgAsReq("prisoner", SPrisoner.class).getPlayer();
            SNormalMine superiorMine = command.getArgAsReq("mine");

            if (!superiorMine.getSpawnPoint().isPresent()) {
                player.sendMessage(ChatColor.RED + "Mine " + superiorMine.getName() + " doesn't have a spawn point!");
                return;
            }

            player.teleport(superiorMine.getSpawnPoint().get().toBukkit());
            player.sendMessage(ChatColor.GREEN + "Teleported to mine " + superiorMine.getName());
        });
    }

}
