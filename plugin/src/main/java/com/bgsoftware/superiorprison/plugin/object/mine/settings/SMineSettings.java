package com.bgsoftware.superiorprison.plugin.object.mine.settings;

import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
import com.bgsoftware.superiorprison.plugin.config.main.MineDefaultsSection;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.google.gson.annotations.SerializedName;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SMineSettings implements Attachable<SNormalMine>, com.bgsoftware.superiorprison.api.data.mine.settings.MineSettings, SerializableObject {

    @SerializedName(value = "playerLimit")
    private int playerLimit;

    @SerializedName(value = "resetSettings")
    private ResetSettings resetSettings;

    private transient SNormalMine mine;

    SMineSettings() {}

    public SMineSettings(MineDefaultsSection defaults) {
        this.playerLimit = defaults.getLimit();
        this.resetSettings = SResetSettings.of(defaults.getResetting());
    }

    @Override
    public void attach(SNormalMine obj) {
        this.mine = obj;
    }

    @Override
    public ResetSettings getResetSettings() {
        return resetSettings;
    }

    public static SMineSettings from(SMineSettings from) {
        SMineSettings settings = new SMineSettings();
        settings.setPlayerLimit(from.getPlayerLimit());
        settings.setResetSettings(SResetSettings.from(from.getResetSettings()));
        return settings;
    }

    @Override
    public void serialize(SerializedData data) {
        data.write("limit", playerLimit);
        data.write("reset", resetSettings);
    }

    @Override
    public void deserialize(SerializedData data) {
        this.playerLimit = data.applyAs("limit", int.class);
        this.resetSettings = SResetSettings.of(data.getElement("reset").get().getAsJsonObject());
    }
}
