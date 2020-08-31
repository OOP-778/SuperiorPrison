package com.bgsoftware.superiorprison.api;

public class SuperiorPrisonAPI {

    private static SuperiorPrison plugin;

    public SuperiorPrisonAPI(SuperiorPrison prisonPlugin) {
        plugin = prisonPlugin;
    }

    public static boolean isEnabled() {
        return plugin != null;
    }

    public static SuperiorPrison getPlugin() {
        return plugin;
    }
}
