package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.commands.backpacks.CmdBackpacks;
import com.bgsoftware.superiorprison.plugin.commands.bombs.CmdBombs;
import com.bgsoftware.superiorprison.plugin.commands.ladder.PrestigeMaxCmd;
import com.bgsoftware.superiorprison.plugin.commands.ladder.RankupMaxCmd;
import com.bgsoftware.superiorprison.plugin.commands.mines.CmdMines;
import com.bgsoftware.superiorprison.plugin.commands.pcp.CmdPrisonerCP;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.CmdPrisoner;
import com.bgsoftware.superiorprison.plugin.commands.sell.SellCommand;
import com.bgsoftware.superiorprison.plugin.commands.top.CmdTop;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.scheme.SchemeHolder;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.yaml.Config;

import java.util.concurrent.TimeUnit;

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

        // Backpacks
        controller.register(new CmdBackpacks());

        controller.register(new CmdTop());

        controller.register(new CmdMine());

        new PermissionsInitializer(controller);

        controller.register(new CmdBombs());

        controller.register(new MainCmd());

        controller.register(new RankupMaxCmd());
        controller.register(new PrestigeMaxCmd());
        //new CommandsPrinter(controller, new File(SuperiorPrisonPlugin.getInstance().getDataFolder(), "commands.txt"));

        // Unregister all similar commands from other plugins
        new OTask()
                .delay(TimeUnit.SECONDS, 1)
                .runnable(controller::unregisterSimilar)
                .execute();
    }
}
