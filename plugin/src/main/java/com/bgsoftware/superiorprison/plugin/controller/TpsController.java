package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.util.TPS;
import com.oop.orangeengine.main.task.OTask;

public class TpsController {

    private volatile double tps;

    public TpsController() {
        new OTask()
            .sync(false)
            .repeat(true)
            .delay(40)
            .runnable(() -> tps = TPS.getCurrentTps())
            .execute();
    }

    public double getLastTps() {
        return tps;
    }
}
