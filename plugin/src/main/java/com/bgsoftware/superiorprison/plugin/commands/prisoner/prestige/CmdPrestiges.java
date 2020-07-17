package com.bgsoftware.superiorprison.plugin.commands.prisoner.prestige;

import com.oop.orangeengine.command.OCommand;

public class CmdPrestiges extends OCommand {
    public CmdPrestiges() {
        label("prestige");
        alias("prestiges");
        description("Add, remove, clear, view prestiges");
        permission("superiorprison.admin");

        subCommand(new CmdInfo());
        subCommand(new CmdSet());
    }
}
