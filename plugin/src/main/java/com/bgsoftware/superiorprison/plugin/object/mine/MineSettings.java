package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.flags.MineFlag;
import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.main.gson.GsonUpdateable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class MineSettings implements Attachable<SNormalMine>, GsonUpdateable, com.bgsoftware.superiorprison.api.data.mine.settings.MineSettings {

    @SerializedName(value = "playerLimit")
    private int playerLimit;

    @SerializedName(value = "flags")
    private Map<MineFlag, Boolean> flags = Maps.newConcurrentMap();

    @SerializedName(value = "resetSettings")
    private ResetSettings resetSettings;

    private transient SNormalMine mine;

    MineSettings() {}

    @Override
    public void attach(SNormalMine obj) {
        this.mine = obj;

        Arrays.stream(MineFlag.values())
                .filter(flag -> !flags.containsKey(flag))
                .forEach(flag -> flags.put(flag, true));
    }

    @Override
    public ResetSettings getResetSettings() {
        return resetSettings;
    }

    @Override
    public Map<MineFlag, Boolean> getFlags() {
        return flags;
    }
}
