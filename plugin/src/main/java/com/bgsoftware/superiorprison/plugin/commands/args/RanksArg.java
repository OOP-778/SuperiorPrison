package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
public class RanksArg extends CommandArgument<Rank> {

  private final boolean ladderOnly;
  private final boolean filterOut;

  public RanksArg(boolean ladderOnly, boolean filterOut) {
    this.ladderOnly = ladderOnly;
    this.filterOut = filterOut;
    setIdentity("rank");
    setDescription("A rank");

    setMapper(
        rankName -> {
          Optional<Rank> first =
              getRanks().stream()
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
    command.nextTabComplete(
        (previous, args) -> {
          SPrisoner prisoner = previous.find(SPrisoner.class).orElse(null);
          return SuperiorPrisonPlugin.getInstance().getRankController().getRanks().stream()
              .filter(rank -> !ladderOnly || rank instanceof LadderRank)
              .filter(rank -> !filterOut || !prisoner.hasRank(rank))
              .map(Rank::getName)
              .collect(Collectors.toList());
        });
  }
}
