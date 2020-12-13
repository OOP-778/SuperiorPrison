package com.bgsoftware.superiorprison.plugin.economy;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.economy.impl.SpEconomy;
import com.bgsoftware.superiorprison.plugin.economy.impl.VaultEconomy;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.oop.orangeengine.main.logger.OLogger;
import com.oop.orangeengine.main.task.StaticTask;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public interface EconomyFramework {
    EconomyFramework framework = requestFramework();

    static EconomyFramework requestFramework() {
        OLogger logger = SuperiorPrisonPlugin.getInstance().getOLogger();
        boolean vaultProvider = true;

        try {
            Class<?> aClass = Class.forName("net.milkbowl.vault.economy.plugins.Economy_SuperiorPrison");
            Class<? extends Economy> aClass1 = SuperiorPrisonPlugin.getInstance().getHookController()
                    .findHook(() -> VaultHook.class)
                    .get()
                    .getEcoProvider().getClass();

            vaultProvider = aClass != aClass1;
        } catch (Throwable throwable) {
            logger.printWarning("===================");
            logger.printWarning("  ");
            logger.printWarning("I have found out you're not using SuperiorPrison Economy!");
            logger.printWarning("It is highly recommended to use it. More information at our wiki");
            logger.printWarning("at https://wiki.bg-software.com/#/superiorprison/");
            logger.printWarning("  ");
            logger.printWarning("===================");
        }

        return vaultProvider ? new VaultEconomy() : new SpEconomy();
    }

    Transaction newDepositer(OfflinePlayer player);

    Transaction newWithdrawer(OfflinePlayer player);

    default void doDepositNow(OfflinePlayer player, BigDecimal decimal, boolean async) {
        doTransactionNow(player, decimal, async, true);
    }

    default void doTransactionNow(OfflinePlayer player, BigDecimal decimal, boolean async, boolean deposit) {
        Runnable withdraw = () -> {
            Transaction transaction = deposit ? newDepositer(player) : newWithdrawer(player);
            transaction.add(decimal);
            transaction.push();
        };

        if (async)
            StaticTask.getInstance().async(withdraw);
        else
            withdraw.run();
    }

    default void doWithdrawNow(OfflinePlayer player, BigDecimal decimal, boolean async) {
        doTransactionNow(player, decimal, async, false);
    }

    BigDecimal getBalance(OfflinePlayer player);
}
