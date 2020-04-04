package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RankController {
    boolean isLoaded();

    LadderRank getDefault();

    Set<Rank> getRanks();

    List<Rank> getSpecialRanks();

    List<LadderRank> getLadderRanks();

    Optional<Rank> getRank(String name);

    Optional<LadderRank> getLadderRank(String name);
}
