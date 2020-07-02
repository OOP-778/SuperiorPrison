package com.bgsoftware.superiorprison.plugin.commands.prisoner.prestige;

import com.oop.orangeengine.command.OCommand;

public class CmdPrestiges extends OCommand {
    public CmdPrestiges() {
        label("prestiges");
        description("Add, remove, clear, view prestiges");
        permission("superiorprison.admin");

        subCommand(new CmdAdd());
        subCommand(new CmdClear());
        subCommand(new CmdList());
        subCommand(new CmdRemove());
    }
}
