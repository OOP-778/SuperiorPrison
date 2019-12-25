package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.version.OVersion;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;
import org.bukkit.entity.Player;

public class CmdEdit extends OCommand {

    public CmdEdit() {
        label("edit");
        argument(new MinesArg().setRequired(true));
        ableToExecute(Player.class);
        permission("superiorprison.admin");
        onCommand(command -> {
            Player player = (Player) command.getSender();
            SNormalMine mine = (SNormalMine) command.getArg("mine").get();

            if (OVersion.isAfter(13)) {
                AMenu menu = SuperiorPrisonPlugin.getInstance().getMenuController().getEditMenu().build(mine);
                WrappedInventory wrappedInventory = menu.getWrappedInventory();
                wrappedInventory.open(player);

            } else
                StaticTask.getInstance().async(() -> {
                    AMenu menu = SuperiorPrisonPlugin.getInstance().getMenuController().getEditMenu().build(mine);
                    WrappedInventory wrappedInventory = menu.getWrappedInventory();
                    wrappedInventory.open(player);
                });
        });
    }

}
