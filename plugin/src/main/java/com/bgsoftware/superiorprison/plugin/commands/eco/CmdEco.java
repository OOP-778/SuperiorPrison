package com.bgsoftware.superiorprison.plugin.commands.eco;

import com.oop.orangeengine.command.OCommand;

public class CmdEco extends OCommand {
    public CmdEco() {
        label("eco");
        description("Main economy command");
        subCommand(new CmdGive());
        subCommand(new CmdSet());
    }
}
