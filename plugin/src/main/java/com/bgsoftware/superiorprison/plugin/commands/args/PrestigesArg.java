package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.api.data.player.LadderObject;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.Testing;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.List;
import java.util.function.Function;

public class PrestigesArg extends CommandArgument<Function<SPrisoner, LadderObject>> {
    public PrestigesArg() {
        setIdentity("prestige");
        setDescription("A prestige");

        setMapper(name -> new OPair<>(Testing.prestigeGenerator.getParser(name).orElse(null), "Failed to find prestige by name " + name));
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete((previous, args) -> {
            List<String> available = Testing.prestigeGenerator.getAvailable();
            SPrisoner prisoner = previous.find(SPrisoner.class).orElse(null);
            if (available.size() > 20) {
                available = available.subList(0, 20);
                available.add("...");
            }

            if (prisoner != null && prisoner.getPrestige() != -1)
                available
                        .removeIf(p -> Integer.parseInt(p) <= prisoner.getPrestige());

            return available;
        });
    }
}
