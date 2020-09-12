package com.bgsoftware.superiorprison.plugin.commands.mines.link;

import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class OptionArg extends CommandArgument<Option> {
    public OptionArg() {
        setIdentity("option");
        setMapper(in -> {
            Optional<Option> typeOptional = Optional.ofNullable(Option.match(in));
            return new OPair<>(typeOptional.orElse(null), "Failed to find option by " + in);
        });
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete((args, e) -> Arrays.stream(Option.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toList()));
    }
}
