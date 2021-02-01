package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.commands.mines.CmdReload;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;

public class MainCmd extends OCommand {
  public MainCmd() {
    label("superiorprison");
    alias("sp", "prison");
    permission("prison.cmds.admin.main");

    subCommand(new CmdReload());
  }

  public void afterRegister(CommandController controller) {
    for (OCommand value : controller.getRegisteredCommands().values()) {
      if (value.getLabel().equalsIgnoreCase(getLabel())) continue;
      subCommand(value.clone());
    }
  }
}
