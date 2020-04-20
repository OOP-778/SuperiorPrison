package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.menu.SellMenu;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

public class SellCommand extends OCommand {
    public SellCommand() {
        label("sellall");
        permission("superiorprison.sellgui");
        ableToExecute(Player.class);
        onCommand(command -> {
            SellMenu sellMenu = new SellMenu(SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(command.getSenderAsPlayer()));
            command.getSenderAsPlayer().openInventory(sellMenu.getInventory());
        });
    }
}
