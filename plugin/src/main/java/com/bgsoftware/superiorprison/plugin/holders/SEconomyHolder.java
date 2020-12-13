package com.bgsoftware.superiorprison.plugin.holders;

import com.bgsoftware.superiorprison.api.controller.EconomyController;
import com.bgsoftware.superiorprison.api.data.account.EconomyAccount;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.economy.EconomyConfig;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.account.SEconomyAccount;
import com.bgsoftware.superiorprison.plugin.util.DualKeyMap;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.google.common.base.Preconditions;
import com.oop.datamodule.universal.UniversalStorage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class SEconomyHolder extends UniversalStorage<SEconomyAccount> implements EconomyController {
    private final DualKeyMap<UUID, String, SEconomyAccount> accounts = DualKeyMap
            .create(
                    1,
                    SEconomyAccount::getUUID,
                    SEconomyAccount::getUsername
            );

    @Setter
    @Getter
    private EconomyConfig config;

    public SEconomyHolder(DatabaseController controller) {
        super(controller);
        config = new EconomyConfig(SuperiorPrisonPlugin.getInstance().getConfigController().getEconomyConfig());
        addVariant("accounts", SEconomyAccount.class);

        currentImplementation(
                SuperiorPrisonPlugin.getInstance().getMainConfig().getStorageSection().provideFor(this)
        );
    }

    @Override
    public Optional<EconomyAccount> findAccountByUUID(UUID uuid) {
        return Optional.ofNullable(accounts.getFirst(uuid));
    }

    @Override
    public Optional<EconomyAccount> findAccountByUsername(String username) {
        return Optional.ofNullable(accounts.getSecond(username));
    }

    @Override
    public EconomyAccount getAccountByUUID(UUID uuid) {
        return accounts.computeIfAbsentByFirst(
                uuid,
                key -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    return new SEconomyAccount(key, offlinePlayer.getName());
                }
        );
    }

    @Override
    public EconomyAccount getAccountByUsername(String username) {
        return accounts.computeIfAbsentBySecond(
                username,
                key -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(key);
                    Preconditions.checkArgument(offlinePlayer != null, "Invalid player passed by username " + username);
                    return new SEconomyAccount(offlinePlayer.getUniqueId(), offlinePlayer.getName());
                }
        );
    }

    @Override
    public String formatMoney(BigDecimal amount) {
        return NumberUtil.formatBigDecimal(amount);
    }

    @Override
    protected void onAdd(SEconomyAccount sEconomyAccount) {
        accounts.put(sEconomyAccount.getUUID(), sEconomyAccount.getUsername(), sEconomyAccount);
    }

    @Override
    protected void onRemove(SEconomyAccount sEconomyAccount) {
        accounts.remove(sEconomyAccount);
    }

    @Override
    public Stream<SEconomyAccount> stream() {
        return accounts.stream();
    }

    @Override
    public Iterator<SEconomyAccount> iterator() {
        return accounts.iterator();
    }
}
