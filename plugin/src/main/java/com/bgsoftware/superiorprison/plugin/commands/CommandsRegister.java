package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.commands.mines.*;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;

public class CommandsRegister {

    private CommandsRegister() {
    }

    public static void register(CommandController controller) {
        // Mines
        controller.register(new OCommand()
                .label("mines")
                .subCommand(new CmdCreate())
                .subCommand(new CmdInfo())
                .subCommand(new CmdTeleport())
                .subCommand(new CmdList())
                .subCommand(new CmdDelete())
                .subCommand(new CmdEdit())
                .subCommand(new CmdReset())
                .subCommand(new CmdBenchmark())
        );
    }

}
