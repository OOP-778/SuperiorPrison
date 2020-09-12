package com.bgsoftware.superiorprison.plugin.menu.settings.impl;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.settings.SettingsObject;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;

public class PlayerLimitSetting extends SettingsObject<Integer> {
    public PlayerLimitSetting(SMineSettings settings) {
        super(Integer.class, settings.getPlayerLimit());
        mapper(string -> {
            try {
                return Integer.parseInt(string);
            } catch (Throwable throwable) {
                throw new IllegalStateException("Invalid int: " + string);
            }
        });
        requestMessage(LocaleEnum.EDIT_SETTINGS_LIMIT.getWithPrefix());
        completeMessage(LocaleEnum.EDIT_SETTINGS_LIMIT_SUCCESS.getWithPrefix());
        onComplete(limit -> {
            settings.setPlayerLimit(limit);
            settings.getMine().getLinker().call(settings);
            settings.getMine().save(true);
        });
        id("player limit");
    }
}
