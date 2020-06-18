package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.PermissionsInitializer;
import com.bgsoftware.superiorprison.plugin.menu.MinesListMenu;
import com.oop.orangeengine.command.OCommand;

public class CmdMines extends OCommand {

    public CmdMines() {
        label("mines");
        onCommand(command -> {
            MinesListMenu minesListMenu = new MinesListMenu(SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(command.getSenderAsPlayer()));
            command.getSenderAsPlayer().openInventory(minesListMenu.getInventory());
        });
        subCommand(new CmdCreate());
        subCommand(new CmdDelete());
        subCommand(new CmdReset());
        subCommand(new CmdReload());
        subCommand(new CmdCopy());

        PermissionsInitializer.registerPrisonerCommand(this);
    }
}
