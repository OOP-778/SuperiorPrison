package com.bgsoftware.superiorprison.plugin.commands.prisoners.boosters;

import com.oop.orangeengine.command.OCommand;

public class CmdBoosters extends OCommand {
    public CmdBoosters() {
        label("boosters");
        description("view, add, remove boosters");
        permission("superiorprison.admin");
    }
}
