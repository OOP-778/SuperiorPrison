package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.commands.backpacks.CmdBackpacks;
import com.bgsoftware.superiorprison.plugin.commands.bombs.CmdBombs;
import com.bgsoftware.superiorprison.plugin.commands.mines.CmdMines;
import com.bgsoftware.superiorprison.plugin.commands.prisoner.CmdPrisoner;
import com.bgsoftware.superiorprison.plugin.commands.rankup.CmdMaxRankup;
import com.bgsoftware.superiorprison.plugin.commands.rankup.CmdRankup;
import com.bgsoftware.superiorprison.plugin.commands.sell.SellCommand;
import com.bgsoftware.superiorprison.plugin.commands.top.CmdTop;
import com.oop.orangeengine.command.OCommand;

public class MainCmd extends OCommand {

    public MainCmd() {
        label("superiorprison");
        alias("sp");
        permission("prison.cmds.admin.main");

        subCommand(new CmdMine());
        subCommand(new CmdTop());
        subCommand(new SellCommand());
        subCommand(new CmdRankup());
        subCommand(new CmdMaxRankup());
        subCommand(new CmdPrisoner());
        subCommand(new CmdMines());
        subCommand(new CmdBombs());
        subCommand(new CmdBackpacks());
    }
}
