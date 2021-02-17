package com.bgsoftware.superiorprison.plugin.commands;

import static com.bgsoftware.superiorprison.plugin.commands.PermissionsInitializer.initPermissions;
import static com.oop.orangeengine.main.Engine.getEngine;

import com.bgsoftware.superiorprison.plugin.commands.backpacks.CmdBackpacks;
import com.bgsoftware.superiorprison.plugin.commands.bombs.CmdBombs;
import com.bgsoftware.superiorprison.plugin.commands.mines.CmdMines;
import com.bgsoftware.superiorprison.plugin.commands.pcp.CmdPrisonerCP;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.CmdPrisoner;
import com.bgsoftware.superiorprison.plugin.commands.rankup.CmdMaxRankup;
import com.bgsoftware.superiorprison.plugin.commands.rankup.CmdRankup;
import com.bgsoftware.superiorprison.plugin.commands.sell.SellCommand;
import com.bgsoftware.superiorprison.plugin.commands.top.CmdTop;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.CommandsRegisterer;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.scheme.SchemeHolder;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.yaml.Config;
import java.util.concurrent.TimeUnit;

public class CommandsRegister {
  public static void register() {
    OFile schemesFile =
        new OFile(getEngine().getOwning().getDataFolder(), "commandScheme.yml")
            .createIfNotExists(true);

    Config config = new Config(schemesFile);
    SchemeHolder schemeHolder = new SchemeHolder(config);
    CommandController controller = new CommandController(schemeHolder);

    MainCmd mainCmd = new MainCmd();

    new CommandsRegisterer(controller)
        .add(mainCmd)
        .add(new CmdMines())
        .add(new SellCommand())
        .add(new CmdPrisoner())
        .add(new CmdPrisonerCP())
        .add(new CmdRankup())
        .add(new CmdMaxRankup())
        .add(new CmdBackpacks())
        .add(new CmdTop())
        .add(new CmdMine())
        .add(new CmdBombs())
        .use(
            cmds -> {
              for (OCommand cmd : cmds) {
                initPermissions("", cmd);
              }
            })
        .remap()
        .push();

    mainCmd.afterRegister(controller);

    // Unregister all similar commands from other plugins
    //new OTask().delay(TimeUnit.SECONDS, 1).runnable(controller::unregisterSimilar).execute();
  }
}
