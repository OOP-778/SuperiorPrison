package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.mines.*;
import com.bgsoftware.superiorprison.plugin.commands.pcp.CmdPrisonerCP;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.CmdTeleport;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.boosters.CmdBoosters;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.prestige.*;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.ranks.CmdRanks;
import com.bgsoftware.superiorprison.plugin.commands.rankup.CmdRankup;
import com.bgsoftware.superiorprison.plugin.commands.sell.SellCommand;
import com.bgsoftware.superiorprison.plugin.menu.MinesListMenu;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.newVersion.SchemeHolder;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.yaml.Config;

import static com.oop.orangeengine.main.Engine.getEngine;

public class CommandsRegister {
    public static void register() {

        OFile schemesFile = new OFile(getEngine().getOwning().getDataFolder(), "commandScheme.yml").createIfNotExists(true);
        Config config = new Config(schemesFile);
        SchemeHolder schemeHolder = new SchemeHolder(config);
        CommandController controller = new CommandController(schemeHolder);

        // Mines
        controller.register(new CmdMines());

        controller.register(new SellCommand());

        // Prisoners
        controller.register(
                new OCommand()
                        .label("prisoner")
                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoner.CmdReset())
                        .subCommand(new CmdTeleport())
                        .subCommand(
                                new CmdRanks()
                                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoner.ranks.CmdAdd())
                                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoner.ranks.CmdRemove())
                                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoner.ranks.CmdClear())
                                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoner.ranks.CmdList())
                        )
                        .subCommand(
                                new CmdBoosters()
                                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoner.boosters.CmdAdd())
                                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoner.boosters.CmdList())
                                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoner.boosters.CmdRemove())
                                        .subCommand(new com.bgsoftware.superiorprison.plugin.commands.prisoner.boosters.CmdClear())
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

        new PermissionsInitializer(controller);
        //new CommandsPrinter(controller, new File(SuperiorPrisonPlugin.getInstance().getDataFolder(), "commands.txt"));
    }
}
