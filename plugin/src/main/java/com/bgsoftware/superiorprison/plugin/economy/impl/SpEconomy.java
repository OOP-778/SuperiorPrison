package com.bgsoftware.superiorprison.plugin.economy.impl;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.economy.Depositer;
import com.bgsoftware.superiorprison.plugin.economy.EconomyFramework;
import com.bgsoftware.superiorprison.plugin.economy.Withdrawer;
import com.bgsoftware.superiorprison.plugin.object.account.SEconomyAccount;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class SpEconomy implements EconomyFramework {
    @Override
    public Depositer newDepositer(OfflinePlayer player) {
        return new SpDepositer((SEconomyAccount) SuperiorPrisonPlugin.getInstance().getEconomyController().getAccountByUUID(player.getUniqueId()));
    }

    @Override
    public Withdrawer newWithdrawer(OfflinePlayer player) {
        return new SpWithdrawer((SEconomyAccount) SuperiorPrisonPlugin.getInstance().getEconomyController().getAccountByUUID(player.getUniqueId()));
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        return SuperiorPrisonPlugin.getInstance().getEconomyController().getAccountByUUID(player.getUniqueId()).getBalance();
    }

    private static class SpDepositer implements Depositer {

        private final SEconomyAccount account;

        private SpDepositer(SEconomyAccount account) {
            this.account = account;
        }

        @Override
        public void push() {
        }

        @Override
        public void add(BigDecimal decimal) {
            account.apply(old -> old.add(decimal));
        }
    }

    private static class SpWithdrawer implements Withdrawer {

        private final SEconomyAccount account;

        private SpWithdrawer(SEconomyAccount account) {
            this.account = account;
        }

        @Override
        public void push() {
        }

        @Override
        public void add(BigDecimal decimal) {
            account.remove(decimal);
        }
    }
}
