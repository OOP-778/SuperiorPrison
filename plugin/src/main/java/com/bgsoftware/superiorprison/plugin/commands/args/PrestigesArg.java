package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrestige;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PrestigesArg extends CommandArgument<SPrestige> {
    public PrestigesArg() {
        setIdentity("prestige");
        setDescription("A prestige");

        setMapper(name -> {
            Optional<Prestige> first = getPrestiges()
                    .stream()
                    .filter(prestige -> prestige.getName().equalsIgnoreCase(name))
                    .findFirst();
            return new OPair<>(first.orElse(null), "Failed to find prestige by name " + name);
        });
    }

    public List<Prestige> getPrestiges() {
        return new ArrayList<>(SuperiorPrisonPlugin.getInstance().getPrestigeController().getPrestiges());
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete(args -> getPrestiges().stream().map(Prestige::getName).collect(Collectors.toList()));
    }
}
