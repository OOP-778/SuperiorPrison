package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SResetSettings;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.main.task.OTask;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class MineResetTask extends OTask {
  public MineResetTask() {
    sync(false);
    repeat(true);
    delay(TimeUnit.SECONDS, 1);
    runnable(
        () -> {
          if (SuperiorPrisonPlugin.disabling) return;
          SuperiorPrisonPlugin.getInstance()
              .getDatabaseController()
              .getMineHolder()
              .getMines(mine -> mine.getSettings().getResetSettings().isTimed())
              .forEach(
                  mine -> {
                    if (mine.getPrisoners().isEmpty()) return;

                    SResetSettings.STimed timed =
                        mine.getSettings().getResetSettings().as(SResetSettings.STimed.class);
                    if (timed.getResetDate() == null)
                      timed.setResetDate(TimeUtil.getDate().plusSeconds(timed.getInterval()));

                    Duration duration = Duration.between(TimeUtil.getDate(), timed.getResetDate());
                    if (duration.getSeconds() <= 0) {
                      mine.getGenerator().reset();
                      timed.setResetDate(null);
                    }
                  });
        });
    execute();
  }
}
