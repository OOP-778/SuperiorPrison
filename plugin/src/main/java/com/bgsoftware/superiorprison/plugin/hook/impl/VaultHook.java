package com.bgsoftware.superiorprison.plugin.hook.impl;

import com.bgsoftware.superiorprison.plugin.hook.SHook;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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

    public void removePermissions(SPrisoner prisoner, List<String> permissions) {
        Objects.requireNonNull(permProvider, "Failed to remove permission, missing permission provider!");
        permissions.forEach(perm -> permProvider.playerRemove(null, prisoner.getOfflinePlayer(), perm));
    }

    public void addPermissions(SPrisoner prisoner, List<String> permissions) {
        Objects.requireNonNull(permProvider, "Failed to add permission, missing permission provider!");
        permissions.forEach(perm -> permProvider.playerAdd(null, prisoner.getOfflinePlayer(), perm));
    }

    public void depositPlayer(SPrisoner prisoner, BigDecimal amount) {
        BigDecimal currentPrice = amount;
        while (currentPrice.compareTo(new BigDecimal(0)) > 0) {
            if (currentPrice.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) > 0) {
                getEcoProvider().depositPlayer(prisoner.getOfflinePlayer(), Double.MAX_VALUE);
                currentPrice = currentPrice.subtract(new BigDecimal(Double.MAX_VALUE));

            } else
                getEcoProvider().depositPlayer(prisoner.getOfflinePlayer(), currentPrice.doubleValue());
        }
    }

    @Override
    public String getPluginName() {
        return "Vault";
    }
}
