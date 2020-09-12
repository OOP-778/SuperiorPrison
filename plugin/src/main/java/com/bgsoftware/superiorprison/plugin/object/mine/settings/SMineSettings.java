package com.bgsoftware.superiorprison.plugin.object.mine.settings;

import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
import com.bgsoftware.superiorprison.api.data.mine.settings.ResetType;
import com.bgsoftware.superiorprison.plugin.config.MineDefaultsSection;
import com.bgsoftware.superiorprison.plugin.menu.settings.SettingsObject;
import com.bgsoftware.superiorprison.plugin.menu.settings.impl.*;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.linkable.LinkableObject;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@AllArgsConstructor
public class SMineSettings implements Attachable<SNormalMine>, com.bgsoftware.superiorprison.api.data.mine.settings.MineSettings, SerializableObject, LinkableObject<SMineSettings> {

    private int playerLimit;
    private ResetSettings resetSettings;
    private transient SNormalMine mine;
    private boolean teleportation = true;
    private boolean disableEnderPearls = false;

    SMineSettings() {
    }

    public SMineSettings(MineDefaultsSection defaults) {
        this.playerLimit = defaults.getLimit();
        this.resetSettings = SResetSettings.of(defaults.getResetting());
        this.teleportation = defaults.isTeleporation();
        this.disableEnderPearls = defaults.isDisableEnderPearls();
    }

    public static SMineSettings from(SMineSettings from) {
        SMineSettings settings = new SMineSettings();
        settings.setPlayerLimit(from.getPlayerLimit());
        settings.setResetSettings(SResetSettings.from(from.getResetSettings()));
        settings.teleportation = from.teleportation;
        settings.disableEnderPearls = from.disableEnderPearls;
        return settings;
    }

    @Override
    public void attach(SNormalMine obj) {
        this.mine = obj;
        ((Attachable<SNormalMine>) resetSettings).attach(mine);
    }

    @Override
    public ResetSettings getResetSettings() {
        return resetSettings;
    }

    @Override
    public void serialize(SerializedData data) {
        data.write("limit", playerLimit);
        data.write("reset", resetSettings);
        data.write("teleportation", teleportation);
        data.write("disableEnderPearls", disableEnderPearls);
    }

    @Override
    public void deserialize(SerializedData data) {
        this.playerLimit = data.applyAs("limit", int.class);
        this.resetSettings = SResetSettings.of(data.getElement("reset").get().getAsJsonObject());
        this.teleportation = data.applyAs("teleportation", boolean.class, () -> true);
        this.disableEnderPearls = data.getChildren("disableEnderPearls")
                .map(sd -> sd.applyAs(boolean.class)).orElse(false);
    }

    public List<SettingsObject> getSettingObjects() {
        List<SettingsObject> objects = new ArrayList<>();
        objects.add(new PlayerLimitSetting(this));
        objects.add(new ResetTypeSetting(this));
        objects.add(new ResetValueSetting(this));
        objects.add(new MineTeleporationSetting(this));
        objects.add(new DisableEnderPearlsSetting(this));
        return objects;
    }

    public void setResetType(ResetType resetType) {
        ResetSettings settings;
        if (resetType == ResetType.PERCENTAGE)
            settings = new SResetSettings.SPercentage(60);
        else
            settings = new SResetSettings.STimed(TimeUnit.MINUTES.toSeconds(10));

        this.resetSettings = settings;
    }

    @SneakyThrows
    @Override
    public void onChange(SMineSettings from) {
        this.teleportation = from.teleportation;
        this.playerLimit = from.playerLimit;
        this.resetSettings = this.resetSettings.clone();
        this.disableEnderPearls = from.disableEnderPearls;
        if (resetSettings instanceof Attachable)
            ((Attachable)this.resetSettings).attach(mine);
    }

    @Override
    public String getLinkId() {
        return "settings";
    }
}
