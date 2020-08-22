package com.bgsoftware.superiorprison.plugin.menu.settings.impl;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.settings.SettingsObject;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;

public class MineTeleporationSetting extends SettingsObject<Boolean> {
    public MineTeleporationSetting(SMineSettings settings) {
        super(Boolean.class, settings.isTeleporation());
        requestMessage(LocaleEnum.EDIT_SETTINGS_LIMIT.getWithPrefix());
        completeMessage(LocaleEnum.EDIT_SETTINGS_LIMIT_SUCCESS.getWithPrefix());
        onComplete(teleportation -> {
            settings.setTeleporation(teleportation);
            settings.getMine().save(true);
        });
        id("teleportation");
    }
}
