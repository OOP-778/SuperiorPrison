package com.bgsoftware.superiorprison.plugin.commands.bombs;

import com.oop.orangeengine.command.OCommand;

public class CmdBombs extends OCommand {
  public CmdBombs() {
    label("bombs");
    subCommand(new CmdGive());
  }
}
