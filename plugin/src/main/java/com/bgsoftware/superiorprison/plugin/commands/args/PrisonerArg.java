package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class PrisonerArg extends CommandArgument<SPrisoner> {
    private final boolean offline;

    public PrisonerArg(boolean offline) {
        this.offline = offline;

        setDescription("A prisoner");
        setIdentity("prisoner");
        setMapper(prisoner -> {
            Optional<Prisoner> prisoner1 = SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(prisoner);
            return new OPair<>(prisoner1.orElse(null), "Failed to find prisoner with name " + prisoner);
        });
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete((previous, args) -> SuperiorPrisonPlugin.getInstance().getPrisonerController().stream()
                .filter(prisoner -> offline || prisoner.isOnline())
                .filter(Objects::nonNull)
                .map(prisoner -> prisoner.getOfflinePlayer().getName())
                .collect(Collectors.toList())
        );
    }
}
