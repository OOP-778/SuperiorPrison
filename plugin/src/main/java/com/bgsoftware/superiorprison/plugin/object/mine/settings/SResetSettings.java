package com.bgsoftware.superiorprison.plugin.object.mine.settings;

import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
import com.bgsoftware.superiorprison.api.data.mine.settings.ResetType;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.lib.google.gson.JsonObject;
import com.oop.orangeengine.main.util.data.pair.OPair;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

public class SResetSettings implements Cloneable {
  public static ResetSettings of(OPair<ResetType, String> data) {
    if (data.getFirst() == ResetType.TIMED) return new STimed(TimeUtil.toSeconds(data.getSecond()));
    else return new SPercentage(Integer.parseInt(data.getSecond().replace("%", "")));
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
    return from instanceof STimed
        ? STimed.from((STimed) from)
        : new SPercentage(from.asPercentage().getValue());
  }

  @Getter
  public static class STimed
      implements ResetSettings.Timed, SerializableObject, Attachable<SNormalMine> {
    private SNormalMine mine;
    private long interval;

    @Setter private ZonedDateTime resetDate;

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
    public String getValueHumanified() {
      return TimeUtil.toString(interval);
    }

    @Override
    public String getCurrentHumanified() {
      return TimeUtil.leftToString(resetDate);
    }

    @Override
    public ResetType getType() {
      return ResetType.TIMED;
    }

    @Override
    public long getValue() {
      return interval;
    }

    @Override
    public void setValue(long value) {
      this.interval = value;
    }

    @Override
    public void serialize(SerializedData data) {
      data.write("interval", interval);
    }

    @Override
    public void deserialize(SerializedData data) {
      this.interval = data.applyAs("interval", long.class);
    }

    @Override
    public void attach(SNormalMine obj) {
      this.mine = obj;
    }

    @Override
    @SneakyThrows
    public STimed clone() {
      STimed clone = (STimed) super.clone();
      clone.resetDate = null;
      return clone;
    }
  }

  @Getter
  @Setter
  public static class SPercentage
      implements ResetSettings.Percentage, SerializableObject, Attachable<SNormalMine> {

    private SNormalMine mine;
    private long requiredPercentage;

    private SPercentage() {}

    public SPercentage(long requiredPercentage) {
      this.requiredPercentage = requiredPercentage;
    }

    @Override
    public ResetType getType() {
      return ResetType.PERCENTAGE;
    }

    @Override
    public String getValueHumanified() {
      return requiredPercentage + "";
    }

    @Override
    public String getCurrentHumanified() {
      return getMine().getGenerator().getBlockData().getPercentageLeft() + "";
    }

    @Override
    public long getValue() {
      return requiredPercentage;
    }

    @Override
    public void setValue(long value) {
      this.requiredPercentage = value;
    }

    @Override
    public void serialize(SerializedData serializedData) {
      serializedData.write("rp", requiredPercentage);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
      this.requiredPercentage = serializedData.applyAs("rp", long.class);
    }

    @Override
    public void attach(SNormalMine obj) {
      this.mine = obj;
    }

    @Override
    @SneakyThrows
    public SPercentage clone() {
      return (SPercentage) super.clone();
    }
  }
}
