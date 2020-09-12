package com.bgsoftware.superiorprison.plugin.menu.settings.impl;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.settings.SettingsObject;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;

public class MineTeleporationSetting extends SettingsObject<Boolean> {
    public MineTeleporationSetting(SMineSettings settings) {
        super(Boolean.class, settings.isTeleportation());
        completeMessage(LocaleEnum.EDIT_SETTINGS_VALUE_SUCCESS.getWithPrefix());
        onComplete(teleportation -> {
            settings.setTeleportation(teleportation);
            settings.getMine().getLinker().call(settings);
            settings.getMine().save(true);
        });
        id("teleportation");
    }
}
