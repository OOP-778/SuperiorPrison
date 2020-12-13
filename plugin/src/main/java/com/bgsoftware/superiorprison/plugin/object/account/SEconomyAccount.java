package com.bgsoftware.superiorprison.plugin.object.account;

import com.bgsoftware.superiorprison.api.data.account.EconomyAccount;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.holders.SEconomyHolder;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.universal.model.UniversalBodyModel;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class SEconomyAccount implements EconomyAccount, UniversalBodyModel {
    private String username;
    private UUID uuid;

    private final AtomicReference<BigDecimal> balance = new AtomicReference<>(new BigDecimal(0));
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public SEconomyAccount(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    private SEconomyAccount() {}

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public BigDecimal getBalance() {
        try {
            lock.readLock().lock();
            return balance.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void setBalance(BigDecimal balance) {
        apply(old -> balance);
    }

    @Override
    public void add(BigDecimal amount) {
        apply(old -> old.add(amount));
    }

    @Override
    public void remove(BigDecimal amount) {
        apply(old -> NumberUtil.max(old.subtract(amount), BigDecimal.ZERO));
    }

    @Override
    public boolean has(BigDecimal amount) {
        try {
            lock.readLock().lock();
            return NumberUtil.isMoreOrEquals(balance.get(), amount);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void apply(Function<BigDecimal, BigDecimal> apply) {
        try {
            lock.writeLock().lock();
            this.balance.set(apply.apply(balance.get()));
            onChange();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public SPrisoner getOwner() {
        return SuperiorPrisonPlugin
                .getInstance()
                .getPrisonerController()
                .getInsertIfAbsent(uuid);
    }

    @Override
    public String[] getStructure() {
        return new String[]{
                "uuid",
                "username",
                "balance"
        };
    }

    private synchronized void onChange() {
        this.balance.getAndUpdate(number -> ((SEconomyHolder) SuperiorPrisonPlugin.getInstance().getEconomyController()).getConfig().getMaxBalance().min(number).stripTrailingZeros());
    }

    @Override
    public String getIdentifierKey() {
        return "uuid";
    }

    @Override
    public String getKey() {
        return uuid.toString();
    }

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("uuid", uuid);
        serializedData.write("username", username);
        serializedData.write("balance", balance.get().toString());
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        this.uuid = serializedData.applyAs("uuid", UUID.class);
        this.username = serializedData.applyAs("username", String.class);
        balance.set(new BigDecimal(serializedData.applyAs("balance", String.class)));
    }

    @Override
    public void save(boolean b, Runnable runnable) {
        ((SEconomyHolder) SuperiorPrisonPlugin.getInstance().getEconomyController())
                .save(this, b, runnable);
    }
}
