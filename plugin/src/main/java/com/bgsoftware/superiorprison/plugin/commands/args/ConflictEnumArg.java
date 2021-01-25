package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.plugin.commands.CmdImport;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ConflictEnumArg extends CommandArgument<CmdImport.ConflictEnum> {
    public ConflictEnumArg() {
        setIdentity("conflict");
        setDescription("What happens on conflict?");
        setMapper(name -> new OPair<>(CmdImport.ConflictEnum.from(name), "Failed to find copy type by: " + name));
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete((previous, args) -> Arrays.stream(CmdImport.ConflictEnum.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toList()));
    }
}