package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.commands.backpacks.CmdBackpacks;
import com.bgsoftware.superiorprison.plugin.commands.mines.*;
import com.bgsoftware.superiorprison.plugin.commands.pcp.CmdPrisonerCP;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.CmdPrisoner;
import com.bgsoftware.superiorprison.plugin.commands.rankup.CmdMaxRankup;
import com.bgsoftware.superiorprison.plugin.commands.rankup.CmdRankup;
import com.bgsoftware.superiorprison.plugin.commands.sell.SellCommand;
import com.bgsoftware.superiorprison.plugin.commands.top.CmdTop;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.scheme.SchemeHolder;
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

        // Sell
        controller.register(new SellCommand());

        // Prisoner
        controller.register(new CmdPrisoner());

        // Prisoner CP
        controller.register(new CmdPrisonerCP());

        // Rankup
        controller.register(new CmdRankup());

        // Max rankup cmd
        controller.register(new CmdMaxRankup());

        // Backpacks
        controller.register(new CmdBackpacks());

        controller.register(new CmdTop());

        controller.register(new CmdMine());

        new PermissionsInitializer(controller);
        //new CommandsPrinter(controller, new File(SuperiorPrisonPlugin.getInstance().getDataFolder(), "commands.txt"));
    }
}
