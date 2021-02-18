package com.bgsoftware.superiorprison.plugin.commands.args;

import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FeatureArg extends CommandArgument<String> {
    private static final List<String> completion = Arrays.asList(
            "autosell",
            "autoburn",
            "fortuneblocks",
            "autopickup"
    );

    public FeatureArg() {
        setIdentity("feature");
        setDescription("A prisoner feature");
        setMapper(in -> {
            Optional<String> first = completion.stream()
                    .filter(it -> it.equalsIgnoreCase(in))
                    .findFirst();
            return new OPair<>(first.orElse(null), "Invalid feature by: " + in);
        });
    }

    @Override
    public void onAdd(OCommand command) {
        command
                .nextTabComplete(($, x) -> completion);
    }
}
