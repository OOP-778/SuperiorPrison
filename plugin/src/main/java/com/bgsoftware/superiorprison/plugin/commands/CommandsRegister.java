package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.mines.*;
import com.bgsoftware.superiorprison.plugin.commands.pcp.CmdPrisonerCP;
import com.bgsoftware.superiorprison.plugin.commands.prisoners.CmdTeleport;
import com.bgsoftware.superiorprison.plugin.commands.prisoners.boosters.CmdBoosters;
import com.bgsoftware.superiorprison.plugin.commands.prisoners.prestige.*;
import com.bgsoftware.superiorprison.plugin.commands.prisoners.ranks.CmdRanks;
import com.bgsoftware.superiorprison.plugin.commands.rankup.CmdRankup;
import com.bgsoftware.superiorprison.plugin.menu.MinesListMenu;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;

public class CommandsRegister {
    public static void register(CommandController controller) {
        // Mines
        controller.register(
                new OCommand()
                .label("mines")
                .onCommand(command -> {
                    MinesListMenu minesListMenu = new MinesListMenu(SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(command.getSenderAsPlayer()));
                    command.getSenderAsPlayer().openInventory(minesListMenu.getInventory());
                })
                .subCommand(new CmdCreate())
                .subCommand(new CmdTeleport())
                .subCommand(new CmdDelete())
                .subCommand(new CmdReset())
                .subCommand(new CmdReload())
        );

        // Prisoners
        controller.register(
                new OCommand()
                .label("prisoners")
                .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoners.CmdReset())
                .subCommand(new CmdTeleport())
                .subCommand(
                        new CmdRanks()
                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoners.ranks.CmdAdd())
                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoners.ranks.CmdRemove())
                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoners.ranks.CmdClear())
                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoners.ranks.CmdList())
                )
                .subCommand(
                        new CmdBoosters()
                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoners.boosters.CmdAdd())
                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoners.boosters.CmdList())
                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoners.boosters.CmdRemove())
                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoners.boosters.CmdClear())
                )
                .subCommand(
                        new CmdPrestiges()
                        .subCommand(new CmdAdd())
                        .subCommand(new CmdRemove())
                        .subCommand(new CmdClear())
                        .subCommand(new CmdList())
                )
        );

        controller.register(new CmdPrisonerCP());
        controller.register(new CmdRankup());
    }
}
