package com.bgsoftware.superiorprison.api.data.account;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Function;

public interface EconomyAccount {
    // Get UUID of the account
    UUID getUUID();

    // Get name of the owner who owns the account
    String getUsername();

    // Get balance of the account
    BigDecimal getBalance();

    // Set balance of the account
    void setBalance(BigDecimal balance);

    // Add certain amount to the account
    void add(BigDecimal amount);

    // Remove certain amount from the account
    void remove(BigDecimal amount);

    // Does user have enough money?
    boolean has(BigDecimal amount);

    // Thread safe function to do modify current balance
    void apply(Function<BigDecimal, BigDecimal> apply);

    // Get owner of this account
    Prisoner getOwner();
}
