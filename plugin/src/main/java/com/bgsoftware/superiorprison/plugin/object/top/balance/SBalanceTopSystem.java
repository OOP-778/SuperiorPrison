package com.bgsoftware.superiorprison.plugin.object.top.balance;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.holders.SEconomyHolder;
import com.bgsoftware.superiorprison.plugin.object.account.SEconomyAccount;
import com.bgsoftware.superiorprison.plugin.object.top.STopSystem;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SBalanceTopSystem extends STopSystem<SEconomyAccount, SBalanceTopEntry> {
    @Override
    public String getName() {
        return "Balance";
    }

    @Override
    protected Comparator<SEconomyAccount> comparator() {
        return Comparator.comparing(SEconomyAccount::getBalance);
    }

    @Override
    protected Stream<SEconomyAccount> stream() {
        return ((SEconomyHolder) SuperiorPrisonPlugin
                .getInstance()
                .getEconomyController())
                .stream();
    }

    @Override
    protected Predicate<SEconomyAccount> filter() {
        return acc -> acc.getBalance().compareTo(BigDecimal.ZERO) != 0;
    }

    @Override
    protected BiFunction<SEconomyAccount, Integer, SBalanceTopEntry> constructor() {
        return (o, p) -> new SBalanceTopEntry(o.getOwner(), o, p);
    }
}
