package com.bgsoftware.superiorprison.plugin.commands.prisoner;

import com.bgsoftware.superiorprison.plugin.commands.prisoner.boosters.CmdBoosters;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.prestige.CmdPrestiges;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.rank.CmdRank;
import com.oop.orangeengine.command.OCommand;

public class CmdPrisoner extends OCommand {
    public CmdPrisoner() {
        label("prisoner");
        subCommand(new CmdReset());
        subCommand(new CmdTeleport());

        subCommand(new CmdBoosters());
        subCommand(new CmdRank());
        subCommand(new CmdPrestiges());
    }
}
