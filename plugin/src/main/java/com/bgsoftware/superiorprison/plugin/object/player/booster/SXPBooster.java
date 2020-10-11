package com.bgsoftware.superiorprison.plugin.object.player.booster;

import com.bgsoftware.superiorprison.api.data.player.booster.XPBooster;

public class SXPBooster extends SBooster implements XPBooster {
    public SXPBooster(int id, long validTill, double rate) {
        super(id, validTill, rate);
    }

    public SXPBooster() {
    }

    @Override
    public String getType() {
        return "xp";
    }
}
