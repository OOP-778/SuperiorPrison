package com.bgsoftware.superiorprison.api.data.player.booster;

public interface Booster {
    // Get id of the booster
    int getId();

    // Returns epoch seconds of when it's expiring
    long getValidTill();

    // Get rate of the booster
    double getRate();

    // Get type of the booster
    String getType();
}
