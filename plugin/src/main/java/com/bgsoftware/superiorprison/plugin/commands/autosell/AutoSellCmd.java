package com.bgsoftware.superiorprison.plugin.commands.autosell;

import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

public class AutoSellCmd extends OCommand {

    public AutoSellCmd() {
        label("autosell");
        permission("superiorprison.autosell");
        ableToExecute(Player.class);
    }

}
