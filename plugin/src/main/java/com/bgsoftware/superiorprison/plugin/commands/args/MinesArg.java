package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MinesArg extends CommandArgument<Collection<SuperiorMine>> {
    public MinesArg() {
        setDescription("Mine target");
        setIdentity("mines");
        setMapper(
            mineName -> {
                if (mineName.equalsIgnoreCase("all"))
                    return new OPair<>(SuperiorPrisonPlugin.getInstance().getMineController().getMines(), null);
                else {
                    List<SuperiorMine> collect = SuperiorPrisonPlugin.getInstance()
                        .getMineController().getMines().stream()
                        .filter(mine -> mine.getName().contains(mineName))
                        .collect(Collectors.toList());
                    return new OPair<>(collect.isEmpty() ? null : collect, "Failed to find mines matching: " + mineName);
                }
            });
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete(
            (previous, args) -> {
                List<String> collect = SuperiorPrisonPlugin.getInstance().getMineController()
                    .getMines().stream()
                    .map(SuperiorMine::getName)
                    .collect(Collectors.toList());
                collect.add("all");
                return collect;
            });
    }
}
