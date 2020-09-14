package com.bgsoftware.superiorprison.plugin.menu.settings.impl;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.settings.SettingsObject;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;

public class DisableMonsterSpawnSetting extends SettingsObject<Boolean> {
    public DisableMonsterSpawnSetting(SMineSettings settings) {
        super(Boolean.class, settings.isDisableMonsterSpawn());
        completeMessage(LocaleEnum.EDIT_SETTINGS_VALUE_SUCCESS.getWithPrefix());
        onComplete(disable -> {
            settings.setDisableMonsterSpawn(disable);
            settings.getMine().getLinker().call(settings);
            settings.getMine().save(true);
        });
        id("disable monster spawn");
    }
}