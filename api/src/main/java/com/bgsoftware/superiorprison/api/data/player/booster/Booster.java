package com.bgsoftware.superiorprison.api.data.player.booster;

public interface Booster {
    int getId();

    // Returns epoch seconds of when it's expiring
    long getValidTill();

    double getRate();
}
