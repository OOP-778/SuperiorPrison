package com.bgsoftware.superiorprison.api;

import com.bgsoftware.superiorprison.api.controller.BackPackController;
import com.bgsoftware.superiorprison.api.controller.MineHolder;
import com.bgsoftware.superiorprison.api.controller.PrisonerHolder;
import com.bgsoftware.superiorprison.api.controller.RankController;
import com.bgsoftware.superiorprison.api.controller.RequirementController;
import com.bgsoftware.superiorprison.api.controller.StatisticsController;
import com.bgsoftware.superiorprison.api.controller.TopController;

public interface SuperiorPrison {
  MineHolder getMineController();

  PrisonerHolder getPrisonerController();

  RankController getRankController();

  RequirementController getRequirementController();

  StatisticsController getStatisticsController();

  BackPackController getBackPackController();

  TopController getTopController();
}
