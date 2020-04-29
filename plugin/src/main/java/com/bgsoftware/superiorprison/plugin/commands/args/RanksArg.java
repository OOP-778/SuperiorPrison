package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Accessors(chain = true, fluent = true)
public class RanksArg extends CommandArgument<Rank> {
    public RanksArg() {
        setIdentity("rank");
        setDescription("A rank");

        setMapper(rankName -> {
            Optional<Rank> first = getRanks()
                    .stream()
                    .filter(rank2 -> rank2.getName().equalsIgnoreCase(rankName))
                    .findFirst();
            return new OPair<>(first.orElse(null), "Failed to find rank by name " + rankName);
        });
    }

    public List<Rank> getRanks() {
        return new ArrayList<>(SuperiorPrisonPlugin.getInstance().getRankController().getRanks());
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete((previous, args) -> {
            SPrisoner prisoner = previous.find(SPrisoner.class).orElse(null);
            Stream<String> stream = SuperiorPrisonPlugin.getInstance().getRankController().getRanks().stream().map(Rank::getName);
            if (prisoner != null)
                stream = stream.filter(rank -> !prisoner.hasRank(rank));

            return stream.collect(Collectors.toList());
        });
    }
}
