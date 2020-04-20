package com.bgsoftware.superiorprison.api;

import com.bgsoftware.superiorprison.api.controller.*;

public interface SuperiorPrison {
    MineHolder getMineController();

    PrisonerHolder getPrisonerController();

    RankController getRankController();

    RequirementController getRequirementController();

    StatisticsController getStatisticsController();
}
