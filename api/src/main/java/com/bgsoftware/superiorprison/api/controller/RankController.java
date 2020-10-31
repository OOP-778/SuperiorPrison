package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.player.LadderObject;

import java.util.List;
import java.util.Optional;

public interface RankController {
    // Get default Ladder Rank (first rank that people get assigned to when joined)
    LadderObject getDefault();

    // Get list by ladder ranks (by order)
    List<LadderObject> getLadderRanks();

    // Get ladder rank by name
    Optional<LadderObject> getLadderRank(String name);
}
