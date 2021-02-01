package com.bgsoftware.superiorprison.plugin.tasks;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.SPair;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.main.task.OTask;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class SellMessageTask extends OTask {
  public SellMessageTask() {
    delay(
        TimeUnit.SECONDS,
        SuperiorPrisonPlugin.getInstance().getMainConfig().getSoldMessageInterval());
    repeat(true);
    runnable(
        () -> {
          for (SPrisoner prisoner : SuperiorPrisonPlugin.getInstance().getPrisonerController()) {
            if (prisoner.getSoldData().getKey().doubleValue() == 0) continue;
            if (!prisoner.isOnline()) continue;

            SPair<BigDecimal, Long> soldData = prisoner.getSoldData();
            messageBuilder(LocaleEnum.SOLD_BLOCKS_MESSAGE.getWithPrefix())
                .replace("{blocks}", soldData.getValue())
                .replace("{time}", TimeUtil.toString(TimeUnit.MILLISECONDS.toSeconds(getDelay())))
                .replace("{money}", soldData.getKey().toString())
                .send(prisoner.getPlayer());
            soldData.setKey(new BigDecimal(0));
            soldData.setValue(0L);
          }
        });
    execute();
  }
}
