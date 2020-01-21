package com.bgsoftware.superiorprison.plugin.commands.pcp;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.menu.PrisonerControlPanel;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

public class CmdPrisonerCP extends OCommand {

    public CmdPrisonerCP() {
        label("pcp");
        alias("prisonercp");

        onCommand(command -> {
            Player senderAsPlayer = command.getSenderAsPlayer();
            PrisonerControlPanel prisonerControlPanel = new PrisonerControlPanel(SuperiorPrisonPlugin.getInstance().getDataController().insertIfAbsent(senderAsPlayer));
            senderAsPlayer.openInventory(prisonerControlPanel.getInventory());
        });
    }

}
