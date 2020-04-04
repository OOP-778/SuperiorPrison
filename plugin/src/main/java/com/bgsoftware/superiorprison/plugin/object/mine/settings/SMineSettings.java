package com.bgsoftware.superiorprison.plugin.object.mine.settings;

import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
import com.bgsoftware.superiorprison.plugin.config.main.MineDefaultsSection;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.main.gson.GsonUpdateable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SMineSettings implements Attachable<SNormalMine>, GsonUpdateable, com.bgsoftware.superiorprison.api.data.mine.settings.MineSettings {

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
}
