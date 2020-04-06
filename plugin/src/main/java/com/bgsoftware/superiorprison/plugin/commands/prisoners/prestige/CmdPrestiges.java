package com.bgsoftware.superiorprison.plugin.commands.prisoners.prestige;

import com.oop.orangeengine.command.OCommand;

public class CmdPrestiges extends OCommand {
    public CmdPrestiges() {
        label("prestiges");
        description("Add, remove, clear, view prestiges");
        permission("superiorprison.admin");
    }
}
