package com.bgsoftware.superiorprison.plugin.commands.backpacks;

import com.oop.orangeengine.command.OCommand;

public class CmdBackpacks extends OCommand {
    public CmdBackpacks() {
        label("backpacks");
        subCommand(new CmdGive());
    }
}
