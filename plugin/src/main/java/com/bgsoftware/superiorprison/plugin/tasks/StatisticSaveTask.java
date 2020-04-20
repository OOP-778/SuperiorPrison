package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.orangeengine.main.task.OTask;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.bgsoftware.superiorprison.plugin.util.TimeUtil.getDate;

public class StatisticSaveTask extends OTask {
    public StatisticSaveTask() {
        sync(false);
        repeat(true);
        delay(TimeUnit.SECONDS, 1);
        runnable(() -> {
            if (SuperiorPrisonPlugin.disabling) return;
            SuperiorPrisonPlugin.getInstance().getStatisticsController()
                    .stream()
                    .forEach(container -> {
                        boolean[] shouldSave = new boolean[]{false};
                        container.getAllStatistics().forEach(statistic -> {
                            if (shouldSave[0] || statistic.getLastUpdated() <= 0) return;

                            Duration duration = Duration.between(getDate(statistic.getLastUpdated()), getDate());
                            System.out.println(duration.getSeconds());
                            if (duration.getSeconds() == 10)
                                shouldSave[0] = true;
                        });
                        if (shouldSave[0]) {
                            System.out.println("Updated");
                            container.save(true);
                        }
                    });
        });
        execute();
    }
}
