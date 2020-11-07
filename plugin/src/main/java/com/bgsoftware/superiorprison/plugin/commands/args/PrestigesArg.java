package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrestige;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrestigesArg extends CommandArgument<SPrestige> {
    public PrestigesArg() {
        setIdentity("prestige");
        setDescription("A prestige");

        setMapper(name -> {
            Optional<Prestige> first = getPrestiges()
                    .stream()
                    .filter(prestige -> prestige.getName().equalsIgnoreCase(name))
                    .findFirst();
            return new OPair<>((SPrestige)first.orElse(null), "Failed to find prestige by name " + name);
        });
    }

    public List<Prestige> getPrestiges() {
        return new ArrayList<>(SuperiorPrisonPlugin.getInstance().getPrestigeController().getPrestiges());
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete((previous, args) -> {
            SPrisoner prisoner = previous.find(SPrisoner.class).orElse(null);
            Stream<Prestige> stream = SuperiorPrisonPlugin.getInstance().getPrestigeController().getPrestiges().stream();
            if (prisoner != null)
                stream = stream.filter(prestige -> !prisoner.hasPrestige(prestige));

            return stream.map(Prestige::getName).collect(Collectors.toList());
        });
    }
}
