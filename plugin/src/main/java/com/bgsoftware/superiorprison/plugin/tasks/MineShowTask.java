package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.particle.OParticle;

import java.util.concurrent.TimeUnit;

public class MineShowTask extends OTask {

    public MineShowTask() {
        repeat(true);
        delay(TimeUnit.SECONDS, 4);
        runnable(() -> {
            SuperiorPrisonPlugin.getInstance().getMineController().getMines().stream().map(obj -> (SNormalMine)obj).forEach(mine -> {

                SArea area = (SArea) mine.getArea(AreaEnum.MINE);
                OParticle.getProvider().display("CLOUD", area.getHighPoint().clone().y(129).toBukkit(), 20);
                OParticle.getProvider().display("CLOUD", area.getMinPoint().clone().y(129).toBukkit(), 20);

            });
        });
        execute();
    }

}
