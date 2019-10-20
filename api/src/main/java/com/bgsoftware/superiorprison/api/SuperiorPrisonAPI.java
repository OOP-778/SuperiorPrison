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

    public static MineController getMineController() {
        return plugin.getMineController();
    }

    public static PrisonerController getPrisonerController() {
        return plugin.getPrisonerController();
    }

}
