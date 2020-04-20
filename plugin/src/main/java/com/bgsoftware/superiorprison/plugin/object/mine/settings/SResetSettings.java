package com.bgsoftware.superiorprison.plugin.object.mine.settings;

import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SResetSettings.STimed;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import com.oop.orangeengine.main.util.data.pair.OPair;
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

    public static ResetSettings of(JsonObject jsonObject) {
        SerializedData data = new SerializedData(jsonObject);
        if (jsonObject.has("rp")) {
            SPercentage percentage = new SPercentage();
            percentage.deserialize(data);
            return percentage;

        } else {
            STimed timed = new STimed();
            timed.deserialize(data);
            return timed;
        }
    }

    public static ResetSettings from(ResetSettings from) {
        return from instanceof STimed ? STimed.from((STimed) from) : new SPercentage(from.asPercentage().getRequiredPercentage());
    }

    @Getter
    public static class STimed implements ResetSettings.Timed, SerializableObject {

        private long interval;

        @Setter
        private transient ZonedDateTime resetDate;

        private STimed() {}

        public STimed(long interval) {
            this.interval = interval;
        }

        public static STimed from(STimed from) {
            STimed timed = new STimed();
            timed.interval = from.interval;
            return timed;
        }

        @Override
        public void setInterval(long interval, TimeUnit unit) {
            this.interval = unit.toSeconds(interval);
        }

        @Override
        public void serialize(SerializedData data) {
            data.write("interval", interval);
        }

        @Override
        public void deserialize(SerializedData data) {
            this.interval = data.applyAs("interval", long.class);
        }
    }

    @Getter
    @Setter
    public static class SPercentage implements ResetSettings.Percentage, SerializableObject {

        @SerializedName(value = "requiredPercentage")
        private int requiredPercentage;

        private SPercentage() {
        }

        private SPercentage(int requiredPercentage) {
            this.requiredPercentage = requiredPercentage;
        }

        @Override
        public void serialize(SerializedData serializedData) {
            serializedData.write("rp", requiredPercentage);
        }

        @Override
        public void deserialize(SerializedData serializedData) {
            this.requiredPercentage = serializedData.applyAs("rp", int.class);
        }
    }
}
