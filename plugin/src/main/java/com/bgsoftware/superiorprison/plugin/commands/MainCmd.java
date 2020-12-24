package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.commands.mines.CmdReload;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;

public class MainCmd extends OCommand {
    private CommandController controller;

    public MainCmd(CommandController controller) {
        this.controller = controller;
        label("superiorprison");
        alias("sp");
        permission("prison.cmds.admin.main");

        subCommand(new CmdReload());
        subCommand(new CmdEval());
    }

    public void onPush() {
        for (OCommand value : controller.getRegisteredCommands().values()) {
            if (value.getLabel().equalsIgnoreCase(getLabel())) continue;

            subCommand(value.clone());
        }
    }
}
