package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.plugin.commands.mines.copy.CopyType;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CopyTypeArg extends CommandArgument<CopyTypeArg> {
    public CopyTypeArg() {
        setIdentity("type");
        setDescription("Copy type");
        setMapper(name -> new OPair<>(CopyType.from(name), "Failed to find copy type by: " + name));
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete(args -> Arrays.stream(CopyType.values()).map(Enum::name).collect(Collectors.toList()));
    }
}