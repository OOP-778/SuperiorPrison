package com.bgsoftware.superiorprison.api.data;

import java.util.UUID;

public interface IPrisoner {

    UUID getUUID();

    boolean isAutoSell();

    IBoosterData getBoosterData();

}
