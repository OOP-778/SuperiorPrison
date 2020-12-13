package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.commands.backpacks.CmdBackpacks;
import com.bgsoftware.superiorprison.plugin.commands.bombs.CmdBombs;
import com.bgsoftware.superiorprison.plugin.commands.mines.CmdMines;
import com.bgsoftware.superiorprison.plugin.commands.mines.CmdReload;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.CmdPrisoner;
import com.bgsoftware.superiorprison.plugin.commands.sell.CmdSell;
import com.bgsoftware.superiorprison.plugin.commands.top.CmdTop;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;

public class MainCmd extends OCommand {
    private CommandController controller;
    public MainCmd(CommandController controller) {
        this.controller = controller;
        label("superiorprison");
        alias("sp");
        permission("prison.cmds.admin.main");
    }

    public void onPush() {
        for (OCommand value : controller.getRegisteredCommands().values()) {
            if (value.getLabel().equalsIgnoreCase(getLabel())) continue;

            subCommand(value.clone());
        }
    }
}
