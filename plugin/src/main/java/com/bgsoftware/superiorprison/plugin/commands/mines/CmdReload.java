package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CmdReload extends OCommand {

    public CmdReload() {
        label("reload");
        permission("superiorprison.reload");
        ableToExecute(Player.class);

        onCommand(command -> {
            try {
                SuperiorPrisonPlugin.getInstance().getPluginComponentController().reload();
                LocaleEnum.PLUGIN_RELOADED.getWithPrefix().send(command.getSenderAsPlayer());
            } catch (Throwable thrw) {
                LocaleEnum.PLUGIN_FAILED_RELOAD.getWithErrorPrefix().send(command.getSenderAsPlayer());
                Bukkit.getPluginManager().disablePlugin(SuperiorPrisonPlugin.getInstance());
                throw new IllegalStateException("Failed to reload SuperiorPrison", thrw);
            }
        });
    }
}
