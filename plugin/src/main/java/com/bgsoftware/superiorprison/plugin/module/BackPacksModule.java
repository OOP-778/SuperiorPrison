package com.bgsoftware.superiorprison.plugin.module;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.backpacks.CmdBackpacks;
import com.bgsoftware.superiorprison.plugin.listeners.BackPackListener;

public class BackPacksModule {
    public static void init() {
        if (isDisabled()) return;

        new BackPackListener();
        SuperiorPrisonPlugin.getInstance().getCommandsRegisterer().register(new CmdBackpacks());
    }

    public static boolean isDisabled() {
        return SuperiorPrisonPlugin.getInstance().getMainConfig().getDisabledModulesSection().contains("backpacks");
    }
}
