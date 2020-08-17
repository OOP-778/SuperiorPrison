package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.messages.SMineMessage;
import com.oop.orangeengine.main.task.OTask;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.bgsoftware.superiorprison.plugin.util.TimeUtil.getDate;

public class MessagesTask extends OTask {

    public MessagesTask() {
        sync(false);
        delay(TimeUnit.SECONDS, 1);
        repeat(true);
        runnable(() -> SuperiorPrisonPlugin.getInstance().getMineController().getMines().forEach(mine -> {
            mine.getMessages().get()
                    .stream()
                    .map(message -> (SMineMessage) message)
                    .forEach(message -> {
                        if (message.getTimeToRun() == null) {
                            message.setTimeToRun(getDate().plusSeconds(message.getInterval()));
                            return;
                        }

                        Duration between = Duration.between(getDate(), message.getTimeToRun());
                        if (between.getSeconds() <= 0) {
                            mine.getPrisoners().forEach(prisoner -> message.send(prisoner.getPlayer()));
                            message.setTimeToRun(getDate().plusSeconds(message.getInterval()));
                        }
                    });
        }));
        execute();
    }
}
