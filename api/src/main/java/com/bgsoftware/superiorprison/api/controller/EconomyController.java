package com.bgsoftware.superiorprison.api.controller;

import com.bgsoftware.superiorprison.api.data.account.EconomyAccount;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface EconomyController {
    Optional<EconomyAccount> findAccountByUUID(UUID uuid);

    Optional<EconomyAccount> findAccountByUsername(String username);

    EconomyAccount getAccountByUUID(UUID uuid);

    EconomyAccount getAccountByUsername(String username);

    String formatMoney(BigDecimal amount);
}
