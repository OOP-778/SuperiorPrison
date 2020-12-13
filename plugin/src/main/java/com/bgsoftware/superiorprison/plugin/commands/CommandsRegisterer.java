package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.bombs.CmdBombs;
import com.bgsoftware.superiorprison.plugin.commands.eco.CmdEco;
import com.bgsoftware.superiorprison.plugin.commands.ladder.PrestigeMaxCmd;
import com.bgsoftware.superiorprison.plugin.commands.ladder.PrestigeUpCmd;
import com.bgsoftware.superiorprison.plugin.commands.ladder.RankupCmd;
import com.bgsoftware.superiorprison.plugin.commands.ladder.RankupMaxCmd;
import com.bgsoftware.superiorprison.plugin.commands.mines.CmdMines;
import com.bgsoftware.superiorprison.plugin.commands.pcp.CmdPrisonerCP;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.CmdPrisoner;
import com.bgsoftware.superiorprison.plugin.commands.sell.CmdSell;
import com.bgsoftware.superiorprison.plugin.commands.top.CmdTop;
import com.bgsoftware.superiorprison.plugin.holders.SEconomyHolder;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.scheme.SchemeHolder;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.yaml.Config;

import java.util.concurrent.TimeUnit;

import static com.oop.orangeengine.main.Engine.getEngine;

public class CommandsRegisterer {
    private final CommandController controller;

    public CommandsRegisterer() {
        OFile schemesFile = new OFile(getEngine().getOwning().getDataFolder(), "commandScheme.yml").createIfNotExists(true);
        Config config = new Config(schemesFile);
        SchemeHolder schemeHolder = new SchemeHolder(config);
        controller = new CommandController(schemeHolder);

        CmdSell cmdSell = new CmdSell();
        MainCmd mainCmd = new MainCmd(controller);
        com.oop.orangeengine.command.CommandsRegisterer registerer = new com.oop.orangeengine.command.CommandsRegisterer(controller)
                .add(new CmdMines())
                .add(cmdSell)
                .add(new CmdPrisoner())
                .add(new CmdPrisonerCP())
                .add(new CmdTop())
                .add(new CmdMine())
                .add(new CmdBombs())
                .add(new RankupMaxCmd())
                .add(new PrestigeMaxCmd())
                .add(new RankupCmd())
                .add(new PrestigeUpCmd())
                .add(mainCmd);



        if (((SEconomyHolder) SuperiorPrisonPlugin.getInstance().getEconomyController()).getConfig().isEnabled())
            registerer.add(new CmdEco());

        registerer.remap();
        registerer.push();

        mainCmd.onPush();
        cmdSell.getInitConosoleCommands().run();

        // Unregister all similar commands from other plugins
        new OTask()
                .delay(TimeUnit.SECONDS, 1)
                .runnable(controller::unregisterSimilar)
                .execute();

        // Permissions initializer
        new PermissionsInitializer(controller);
    }

    public void register(OCommand command) {
        controller.register(command);
    }
}
