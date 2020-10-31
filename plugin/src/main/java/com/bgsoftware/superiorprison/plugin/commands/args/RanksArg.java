package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.Testing;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.function.Function;

@Accessors(chain = true, fluent = true)
public class RanksArg extends CommandArgument<Function<SPrisoner, ParsedObject>> {
    public RanksArg() {
        setIdentity("rank");
        setDescription("A rank");
        setMapper(name -> new OPair<>(Testing.ranksGenerator.getParser(name).orElse(null), "Failed to find prestige by name " + name));
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete((previous, args) -> {
            List<String> available = Testing.ranksGenerator.getAvailable();

            if (available.size() > 20) {
                available = available.subList(0, 20);
                available.add("...");
            }

            return available;
        });
    }
}
