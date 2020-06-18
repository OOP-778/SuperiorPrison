package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CmdReload extends OCommand {

    public CmdReload() {
        label("reload");
        description("reload the plugin");
        permission("superiorprison.reload");

        onCommand(command -> {
            try {
                SuperiorPrisonPlugin.getInstance().getPluginComponentController().reload();
                LocaleEnum.PLUGIN_RELOADED.getWithPrefix().send(command.getSender());
            } catch (Throwable thrw) {
                LocaleEnum.PLUGIN_FAILED_RELOAD.getWithErrorPrefix().send(command.getSender());
                Bukkit.getPluginManager().disablePlugin(SuperiorPrisonPlugin.getInstance());
                throw new IllegalStateException("Failed to reload SuperiorPrison", thrw);
            }
        });
    }
}
