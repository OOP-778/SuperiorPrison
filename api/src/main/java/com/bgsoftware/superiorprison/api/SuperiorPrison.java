package com.bgsoftware.superiorprison.api;

import com.bgsoftware.superiorprison.api.controller.MineHolder;
import com.bgsoftware.superiorprison.api.controller.PrisonerHolder;
import com.bgsoftware.superiorprison.api.controller.RankController;
import com.bgsoftware.superiorprison.api.controller.RequirementController;

public interface SuperiorPrison {

    MineHolder getMineController();

    PrisonerHolder getPrisonerController();

    RankController getRankController();

    RequirementController getRequirementController();
}
