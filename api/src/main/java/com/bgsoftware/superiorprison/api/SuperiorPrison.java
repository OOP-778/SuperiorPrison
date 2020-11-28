package com.bgsoftware.superiorprison.api;

import com.bgsoftware.superiorprison.api.controller.*;

public interface SuperiorPrison {
    MineHolder getMineController();

    PrisonerHolder getPrisonerController();

    StatisticsController getStatisticsController();

    BackPackController getBackPackController();

    TopController getTopController();

    BlockController getBlockController();
}
