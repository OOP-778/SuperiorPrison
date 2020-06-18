package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RankController {
    boolean isLoaded();

    // Get default Ladder Rank (first rank that people get assigned to when joined)
    LadderRank getDefault();

    // Get set of all ranks
    Set<Rank> getRanks();

    // Get list of Special Ranks
    List<Rank> getSpecialRanks();

    // Get list by ladder ranks (by order)
    List<LadderRank> getLadderRanks();

    // Get rank by name
    Optional<Rank> getRank(String name);

    // Get ladder rank by name
    Optional<LadderRank> getLadderRank(String name);
}
