package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SResetSettings;
import com.oop.orangeengine.main.task.OTask;

import java.util.concurrent.TimeUnit;

public class MineResetTask extends OTask {

    public MineResetTask() {
        sync(false);
        repeat(true);
        delay(TimeUnit.SECONDS, 1);
        runnable(() -> SuperiorPrisonPlugin.getInstance().getDatabaseController().getMineHolder().dataBy(mine -> mine.getSettings().getResetSettings().isTimed()).forEach(mine -> {
            SResetSettings.STimed timed = mine.getSettings().getResetSettings().as(SResetSettings.STimed.class);
            timed.setTillReset(timed.getTillReset() - 1);
            if (timed.getTillReset() != 0) return;

            timed.setTillReset(timed.getInterval());
            mine.getGenerator().reset();
        }));
        execute();
    }

}
