package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.particle.OParticle;

import java.util.concurrent.TimeUnit;

public class MineShowTask extends OTask {

    public MineShowTask() {
        repeat(true);
        delay(TimeUnit.SECONDS, 1);
        runnable(() -> {
            SuperiorPrisonPlugin.getInstance().getMineController().getMines().forEach(mine -> {

                OParticle.getProvider().display("CLOUD", mine.getHighPoint().clone().y(129).toBukkit(), 20);
                OParticle.getProvider().display("CLOUD", mine.getMinPoint().clone().y(129).toBukkit(), 20);

            });
        });
        execute();
    }

}
