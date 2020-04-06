package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.menu.SellMenu;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

public class SellCommand extends OCommand {
    public SellCommand() {
        label("sell");
        permission("superiorprison.sellgui");
        ableToExecute(Player.class);
        onCommand(command -> new SellMenu(SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(command.getSenderAsPlayer())));
    }
}
