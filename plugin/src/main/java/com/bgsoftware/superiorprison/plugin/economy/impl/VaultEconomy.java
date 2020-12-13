package com.bgsoftware.superiorprison.plugin.economy.impl;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.economy.Depositer;
import com.bgsoftware.superiorprison.plugin.economy.EconomyFramework;
import com.bgsoftware.superiorprison.plugin.economy.Withdrawer;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class VaultEconomy implements EconomyFramework {
    protected static BigDecimal MAX_DOUBLE = BigDecimal.valueOf(Double.MAX_VALUE);

    private static final Economy ecoProvider = SuperiorPrisonPlugin.getInstance().getHookController()
            .findHook(() -> VaultHook.class)
            .get()
            .getEcoProvider();

    @Override
    public Depositer newDepositer(OfflinePlayer player) {
        return new VaultDepositer(player);
    }

    @Override
    public Withdrawer newWithdrawer(OfflinePlayer player) {
        return new VaultWithdrawer(player);
    }

    private static class VaultDepositer implements Depositer {
        private BigDecimal toDeposit = new BigDecimal(0);
        private final OfflinePlayer player;
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        public VaultDepositer(OfflinePlayer player) {
            this.player = player;
        }

        @Override
        public void push() {
            try {
                lock.readLock().lock();

                Economy ecoProvider = SuperiorPrisonPlugin.getInstance().getHookController()
                        .findHook(() -> VaultHook.class)
                        .get()
                        .getEcoProvider();

                BigDecimal deposited = toDeposit;
                while (toDeposit.compareTo(BigDecimal.ZERO) > 0) {
                    if (toDeposit.compareTo(MAX_DOUBLE) > 0) {
                        ecoProvider.depositPlayer(player, Double.MAX_VALUE);
                        toDeposit = toDeposit.subtract(MAX_DOUBLE);

                    } else {
                        ecoProvider.depositPlayer(player, toDeposit.doubleValue());
                        break;
                    }
                }

                try {
                    lock.writeLock().lock();
                    toDeposit = toDeposit.subtract(deposited);
                } finally {
                    lock.writeLock().unlock();
                }

            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public void add(BigDecimal decimal) {
            try {
                lock.writeLock().lock();
                toDeposit = toDeposit.add(decimal);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    private static class VaultWithdrawer implements Withdrawer {
        private BigDecimal toWithdraw = new BigDecimal(0);
        private final OfflinePlayer player;
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        public VaultWithdrawer(OfflinePlayer player) {
            this.player = player;
        }

        @Override
        public void push() {
            try {
                lock.readLock().lock();

                BigDecimal withdrew = toWithdraw;
                while (toWithdraw.compareTo(BigDecimal.ZERO) > 0) {
                    if (toWithdraw.compareTo(MAX_DOUBLE) > 0) {
                        ecoProvider.withdrawPlayer(player, Double.MAX_VALUE);
                        toWithdraw = toWithdraw.subtract(MAX_DOUBLE);

                    } else {
                        ecoProvider.withdrawPlayer(player, toWithdraw.doubleValue());
                        break;
                    }
                }

                try {
                    lock.writeLock().lock();
                    toWithdraw = toWithdraw.subtract(withdrew);
                } finally {
                    lock.writeLock().unlock();
                }

            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public void add(BigDecimal decimal) {
            try {
                lock.writeLock().lock();
                toWithdraw = toWithdraw.add(decimal);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        return BigDecimal.valueOf(ecoProvider.getBalance(player));
    }
}
