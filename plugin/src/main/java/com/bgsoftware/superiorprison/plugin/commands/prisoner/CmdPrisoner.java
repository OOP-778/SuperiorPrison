package com.bgsoftware.superiorprison.plugin.commands.prisoner;

import com.bgsoftware.superiorprison.plugin.commands.prisoner.boosters.CmdBoosters;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.prestige.CmdPrestiges;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.ranks.CmdRanks;
import com.oop.orangeengine.command.OCommand;

public class CmdPrisoner extends OCommand {
  public CmdPrisoner() {
    label("prisoner");
    subCommand(new CmdReset());
    subCommand(new CmdTeleport());

    subCommand(new CmdBoosters());
    subCommand(new CmdRanks());
    subCommand(new CmdPrestiges());
    subCommand(new CmdToggleFeature());
  }
}
