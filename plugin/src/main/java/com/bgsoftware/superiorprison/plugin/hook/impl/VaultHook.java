package com.bgsoftware.superiorprison.plugin.hook.impl;

import com.bgsoftware.superiorprison.plugin.hook.SHook;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

@Getter
public class VaultHook extends SHook {

    private Economy ecoProvider;
    private Permission permProvider;

    public VaultHook() {
        super(null);
        setRequired(true);

        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null && permissionProvider.getProvider() != null)
            this.permProvider = permissionProvider.getProvider();

        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null && economyProvider.getProvider() != null)
            this.ecoProvider = economyProvider.getProvider();

        disableIf(ecoProvider == null, "Failed to initialize Economy provider!");
    }

    @Override
    public String getPluginName() {
        return "Vault";
    }
}
