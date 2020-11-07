package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.Optional;
import java.util.stream.Collectors;

public class MinesArg extends CommandArgument<SNormalMine> {
    public MinesArg() {
        setDescription("A mine");
        setIdentity("mine");
        setMapper(mineName -> {
            Optional<SuperiorMine> superiorMine = SuperiorPrisonPlugin.getInstance().getMineController().getMines().stream()
                    .filter(mine -> mine.getName().equalsIgnoreCase(mineName))
                    .findFirst();
            return new OPair<>((SNormalMine) superiorMine.orElse(null), "Failed to find mine with name " + mineName);
        });
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete((previous, args) -> SuperiorPrisonPlugin.getInstance().getMineController().getMines().stream()
                .map(SuperiorMine::getName)
                .collect(Collectors.toList())
        );
    }
}
