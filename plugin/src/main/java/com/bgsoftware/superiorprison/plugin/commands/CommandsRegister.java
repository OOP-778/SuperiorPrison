package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.commands.mines.CmdCreate;
import com.bgsoftware.superiorprison.plugin.commands.mines.CmdInfo;
import com.bgsoftware.superiorprison.plugin.commands.mines.CmdTeleport;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;

public class CommandsRegister {

    private CommandsRegister() {
    }

    public static void register(CommandController controller) {
        // Mines
        controller.register(new OCommand().label("mines")
                .subCommand(new CmdCreate())
                .subCommand(new CmdInfo())
                .subCommand(new CmdTeleport())
        );
    }

}
