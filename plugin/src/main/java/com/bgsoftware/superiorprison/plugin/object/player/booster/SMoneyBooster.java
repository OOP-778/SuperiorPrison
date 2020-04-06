package com.bgsoftware.superiorprison.plugin.object.player.booster;

import com.bgsoftware.superiorprison.api.data.player.booster.MoneyBooster;

public class SMoneyBooster extends SBooster implements MoneyBooster {
    public SMoneyBooster(int id, long validTill, double rate) {
        super(id, validTill, rate);
    }
}
