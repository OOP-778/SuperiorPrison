package com.bgsoftware.superiorprison.plugin.commands.sell;

import com.oop.orangeengine.command.OCommand;

public class HandCmd extends OCommand {

    public HandCmd() {
        label("hand");
        permission("superiorprison.autosell.hand");
        onCommand(command -> {


        });
    }

}
