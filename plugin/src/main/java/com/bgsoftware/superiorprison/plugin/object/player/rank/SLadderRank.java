package com.bgsoftware.superiorprison.plugin.object.player.rank;

import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SLadderRank extends SRank implements LadderRank {

  private final int order;
  private final Set<RequirementData> requirements;
  @Setter private SLadderRank nextRank;
  @Setter private SLadderRank previousRank;

  public SLadderRank(
      int order,
      String name,
      String prefix,
      List<String> commands,
      List<String> permissions,
      Set<RequirementData> requirements,
      SLadderRank previousRank,
      SLadderRank nextRank) {
    super(name, prefix, commands, permissions);

    this.order = order;
    this.requirements = requirements;
    this.previousRank = previousRank;
    this.nextRank = nextRank;
  }

  @Override
  public Optional<LadderRank> getNext() {
    return Optional.ofNullable(nextRank);
  }

  @Override
  public Optional<LadderRank> getPrevious() {
    return Optional.ofNullable(previousRank);
  }

  @Override
  public List<LadderRank> getAllPrevious() {
    List<LadderRank> ranks = new ArrayList<>();
    getPrevious().map(rank -> (SLadderRank) rank).ifPresent(rank -> rank._getAllPrevious(ranks));
    return ranks;
  }

  public List<LadderRank> getAllNext() {
    List<LadderRank> ranks = new ArrayList<>();
    getNext().map(rank -> (SLadderRank) rank).ifPresent(rank -> rank._getAllNext(ranks));

    return ranks;
  }

  private void _getAllNext(List<LadderRank> ranks) {
    ranks.add(this);
    getNext().map(rank -> (SLadderRank) rank).ifPresent(rank -> rank._getAllPrevious(ranks));
  }

  private void _getAllPrevious(List<LadderRank> ranks) {
    ranks.add(this);
    getPrevious().map(rank -> (SLadderRank) rank).ifPresent(rank -> rank._getAllPrevious(ranks));
  }
}
