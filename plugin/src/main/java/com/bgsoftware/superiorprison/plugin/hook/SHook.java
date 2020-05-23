package com.bgsoftware.superiorprison.plugin.hook;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public abstract class SHook {
    private @NonNull
    final JavaPlugin plugin;
    private boolean loaded;

    @Setter
    private boolean required;

    public SHook(JavaPlugin plugin) {
        if (plugin == null)
            plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(getPluginName());

        loaded = plugin != null;
        this.plugin = plugin;
    }

    public abstract String getPluginName();

    public void disableIf(boolean b, String s) {
        SuperiorPrisonPlugin.getInstance().getHookController().disableIf(this, b, s);
        if (b)
            loaded = false;
    }
}
