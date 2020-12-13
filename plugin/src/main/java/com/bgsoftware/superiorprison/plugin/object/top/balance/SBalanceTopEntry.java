package com.bgsoftware.superiorprison.plugin.object.top.balance;

import com.bgsoftware.superiorprison.plugin.object.account.SEconomyAccount;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.top.STopEntry;

public class SBalanceTopEntry extends STopEntry<SEconomyAccount> {
    public SBalanceTopEntry(SPrisoner prisoner, SEconomyAccount object, int position) {
        super(prisoner, object, position);
    }
}
