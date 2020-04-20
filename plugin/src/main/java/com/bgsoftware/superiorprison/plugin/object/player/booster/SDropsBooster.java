package com.bgsoftware.superiorprison.plugin.object.player.booster;

import com.bgsoftware.superiorprison.api.data.player.booster.DropsBooster;

public class SDropsBooster extends SBooster implements DropsBooster {
    public SDropsBooster(int id, long validTill, double rate) {
        super(id, validTill, rate);
    }

    public SDropsBooster() {}

}
