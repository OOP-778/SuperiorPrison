package com.bgsoftware.superiorprison.plugin.commands.prisoner.rank;

import com.oop.orangeengine.command.OCommand;

public class CmdRank extends OCommand {
    public CmdRank() {
        label("rank");
        description("set, reset rank");
        permission("superiorprison.admin");

        subCommand(new CmdSet());
        subCommand(new CmdReset());
    }
}
