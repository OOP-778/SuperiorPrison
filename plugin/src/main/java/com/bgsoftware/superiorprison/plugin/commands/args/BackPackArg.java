package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.BackPackConfig;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.Optional;

public class BackPackArg extends CommandArgument<BackPackConfig<?>> {
    public BackPackArg() {
        setDescription("a backpack identifier");
        setIdentity("backpack");
        setMapper(name -> {
            Optional<BackPackConfig<?>> config = SuperiorPrisonPlugin.getInstance().getBackPackController().getConfig(name);
            return new OPair<>(config.orElse(null), "Failed to find a backpack with name " + name);
        });
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete((previous, args) -> SuperiorPrisonPlugin.getInstance().getBackPackController().getConfigs().keySet());
    }
}
