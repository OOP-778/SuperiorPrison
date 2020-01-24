package com.bgsoftware.superiorprison.plugin.hook;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public abstract class SHook {

    private JavaPlugin plugin;
    private boolean loaded;

    @Setter
    private boolean required;

    public SHook(JavaPlugin plugin) {
        this.plugin = plugin;
        if (plugin == null)
            this.plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(getPluginName());
        loaded = true;
    }

    public abstract String getPluginName();

    public void disableIf(boolean b, String s) {
        SuperiorPrisonPlugin.getInstance().getHookController().disableIf(this, b, s);
        if (b)
            loaded = false;
    }
}
