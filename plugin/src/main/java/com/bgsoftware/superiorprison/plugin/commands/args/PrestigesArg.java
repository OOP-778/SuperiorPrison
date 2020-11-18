package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PrestigesArg extends CommandArgument<Function<SPrisoner, ParsedObject>> {
    public PrestigesArg() {
        setIdentity("prestige");
        setDescription("A prestige");

        setMapper(name -> new OPair<>(SuperiorPrisonPlugin.getInstance().getPrestigeController().getParser(name).orElse(null), "Failed to find prestige by name " + name));
    }

}
