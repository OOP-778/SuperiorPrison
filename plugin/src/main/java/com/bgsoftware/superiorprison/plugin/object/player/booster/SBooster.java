package com.bgsoftware.superiorprison.plugin.object.player.booster;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@EqualsAndHashCode
public abstract class SBooster implements Booster {

    @Getter
    @Setter
    @SerializedName(value = "id")
    private int id;

    @Getter
    @SerializedName(value = "validTill")
    private long validTill;

    @Getter
    @SerializedName(value = "rate")
    private double rate;
}
