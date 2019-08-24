package com.bgsoftware.superiorprison.api;

import com.bgsoftware.superiorprison.api.controller.IMineController;
import com.bgsoftware.superiorprison.api.controller.IPrisonerController;

public class SuperiorPrisonAPI {

    private static SuperiorPrison plugin;

    public SuperiorPrisonAPI(SuperiorPrison prisonPlugin) {
        plugin = prisonPlugin;
    }

    public static boolean isEnabled() {
        return plugin != null;
    }

    public IMineController getMineController() {
        return plugin.getMineController();
    }

    public IPrisonerController getPrisonerController() {
        return plugin.getPrisonerController();
    }

}
