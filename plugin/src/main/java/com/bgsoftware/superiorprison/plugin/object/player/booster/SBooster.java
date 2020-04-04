package com.bgsoftware.superiorprison.plugin.object.player.booster;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class SBooster implements Booster {

    @Getter
    private long validTill;

    @Getter
    private double rate;

}
