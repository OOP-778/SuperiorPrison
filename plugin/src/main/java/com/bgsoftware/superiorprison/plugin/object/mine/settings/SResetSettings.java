package com.bgsoftware.superiorprison.plugin.object.mine.settings;

import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.main.gson.GsonUpdateable;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

public class SResetSettings {
    public static ResetSettings of(OPair<ResetSettings.Type, String> data) {
        if (data.getFirst() == ResetSettings.Type.TIMED)
            return new STimed(TimeUtil.toSeconds(data.getSecond()));

        else
            return new SPercentage(Integer.parseInt(data.getSecond().replace("%", "")));
    }

    @Getter
    public static class STimed implements ResetSettings.Timed, GsonUpdateable {

        private long interval;

        @Setter
        private long tillReset;

        private STimed() {}

        public STimed(long interval) {
            this.interval = interval;
            tillReset = interval;
        }

        @Override
        public void setInterval(long interval, TimeUnit unit) {
            this.interval = unit.toSeconds(interval);
            tillReset = this.interval;
        }
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class SPercentage implements ResetSettings.Percentage, GsonUpdateable {
        private int requiredPercentage;

        private SPercentage() {}
    }
}
