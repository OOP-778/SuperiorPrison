package com.bgsoftware.superiorprison.api;

import com.bgsoftware.superiorprison.api.controller.MineController;
import com.bgsoftware.superiorprison.api.controller.PrisonerController;

public interface SuperiorPrison {

    MineController getMineController();

    PrisonerController getPrisonerController();
}
