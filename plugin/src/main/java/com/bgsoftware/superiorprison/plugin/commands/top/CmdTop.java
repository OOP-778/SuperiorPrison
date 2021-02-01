package com.bgsoftware.superiorprison.plugin.commands.top;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.args.TopTypeArg;
import com.bgsoftware.superiorprison.plugin.menu.top.TopMenu;
import com.bgsoftware.superiorprison.plugin.object.top.STopSystem;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

public class CmdTop extends OCommand {
  public CmdTop() {
    label("top");
    argument(new TopTypeArg().setRequired(true));
    ableToExecute(Player.class);
    onCommand(
        command -> {
          TopTypeArg.TopType topType = command.getArgAsReq("type");
          STopSystem system =
              (STopSystem)
                  SuperiorPrisonPlugin.getInstance()
                      .getTopController()
                      .getSystem(topType.getClazz());

          TopMenu topMenu =
              new TopMenu(
                  SuperiorPrisonPlugin.getInstance()
                      .getPrisonerController()
                      .getInsertIfAbsent(command.getSenderAsPlayer()),
                  topType,
                  system);
          command.getSenderAsPlayer().openInventory(topMenu.getInventory());
        });
  }
}
