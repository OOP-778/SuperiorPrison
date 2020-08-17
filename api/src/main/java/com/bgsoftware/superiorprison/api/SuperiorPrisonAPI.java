package com.bgsoftware.superiorprison.api;

import com.bgsoftware.superiorprison.api.controller.*;

public class SuperiorPrisonAPI {

    private static SuperiorPrison plugin;

    public SuperiorPrisonAPI(SuperiorPrison prisonPlugin) {
        plugin = prisonPlugin;
    }

    public static boolean isEnabled() {
        return plugin != null;
    }

    public static MineHolder getMineController() {
        return plugin.getMineController();
    }

    public static PrisonerHolder getPrisonerController() {
        return plugin.getPrisonerController();
    }

    public static RankController getRankController() {
        return plugin.getRankController();
    }

    public static RequirementController getRequirementController() {
        return plugin.getRequirementController();
    }

    public static SuperiorPrison getSuperiorPrison() { return plugin; }

}
