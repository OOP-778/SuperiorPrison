package com.bgsoftware.superiorprison.plugin.object.mine.settings;

import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SResetSettings.STimed;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.main.gson.GsonUpdateable;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class SResetSettings {
    public static ResetSettings of(OPair<ResetSettings.Type, String> data) {
        if (data.getFirst() == ResetSettings.Type.TIMED)
            return new STimed(TimeUtil.toSeconds(data.getSecond()));

        else
            return new SPercentage(Integer.parseInt(data.getSecond().replace("%", "")));
    }

    public static ResetSettings from(ResetSettings from) {
        return from instanceof STimed ? STimed.from((STimed) from) : new SPercentage(from.asPercentage().getRequiredPercentage());
    }

    @Getter
    public static class STimed implements ResetSettings.Timed, GsonUpdateable {

        private long interval;

        @Setter
        private transient ZonedDateTime resetDate;

        private STimed() {}

        public STimed(long interval) {
            this.interval = interval;
        }

        @Override
        public void setInterval(long interval, TimeUnit unit) {
            this.interval = unit.toSeconds(interval);
        }

        public static STimed from(STimed from) {
            STimed timed = new STimed();
            timed.interval = from.interval;
            return timed;
        }

    }

    @Getter
    @Setter
    public static class SPercentage implements ResetSettings.Percentage, GsonUpdateable {
        private int requiredPercentage;

        private SPercentage() {}

        private SPercentage(int requiredPercentage) {
            this.requiredPercentage = requiredPercentage;
        }
    }
}
