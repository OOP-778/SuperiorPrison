package com.bgsoftware.superiorprison.api;

import com.bgsoftware.superiorprison.api.controller.IMineController;
import com.bgsoftware.superiorprison.api.controller.IPrisonerController;

public interface SuperiorPrison {

    IMineController getMineController();

    IPrisonerController getPrisonerController();
}
