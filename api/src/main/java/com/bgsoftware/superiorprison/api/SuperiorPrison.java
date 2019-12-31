package com.bgsoftware.superiorprison.api;

import com.bgsoftware.superiorprison.api.controller.MineController;
import com.bgsoftware.superiorprison.api.controller.PrisonerController;
import com.bgsoftware.superiorprison.api.controller.RankController;
import com.bgsoftware.superiorprison.api.controller.RequirementController;

public interface SuperiorPrison {

    MineController getMineController();

    PrisonerController getPrisonerController();

    RankController getRankController();

    RequirementController getRequirementController();
}
