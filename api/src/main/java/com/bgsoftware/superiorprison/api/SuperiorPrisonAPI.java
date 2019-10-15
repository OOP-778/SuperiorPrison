package com.bgsoftware.superiorprison.api;

import com.bgsoftware.superiorprison.api.controller.MineController;
import com.bgsoftware.superiorprison.api.controller.PrisonerController;

public class SuperiorPrisonAPI {

    private static SuperiorPrison plugin;

    public SuperiorPrisonAPI(SuperiorPrison prisonPlugin) {
        plugin = prisonPlugin;
    }

    public static boolean isEnabled() {
        return plugin != null;
    }

    public MineController getMineController() {
        return plugin.getMineController();
    }

    public PrisonerController getPrisonerController() {
        return plugin.getPrisonerController();
    }

}
