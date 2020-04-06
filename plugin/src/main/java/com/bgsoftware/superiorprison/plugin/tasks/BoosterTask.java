package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.main.task.OTask;

import java.util.concurrent.TimeUnit;

public class BoosterTask extends OTask {
    public BoosterTask() {
        sync(false);
        delay(TimeUnit.SECONDS, 1);
        repeat(true);
        runnable(() -> {
            if (SuperiorPrisonPlugin.disabling) return;
            SuperiorPrisonPlugin.getInstance().getPrisonerController()
                    .dataStream()
                    .parallel()
                    .forEach(prisoner -> {
                        for (Booster booster : prisoner.getBoosters().set()) {
                            if (booster.getValidTill() == -1) continue;

                            if (TimeUtil.hasExpired(booster.getValidTill()))
                                prisoner.getBoosters().removeBooster(booster);
                        }
                    });
        });
        execute();
    }
}
