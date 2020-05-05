package com.bgsoftware.superiorprison.plugin.menu.settings.impl;

import com.bgsoftware.superiorprison.api.data.mine.settings.ResetType;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.settings.SettingsObject;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;

public class ResetTypeSetting extends SettingsObject<ResetType> {
    public ResetTypeSetting(SMineSettings settings) {
        super(ResetType.class, settings.getResetSettings().getType());
        mapper(string -> {
            try {
                return ResetType.valueOf(string.toUpperCase());
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to find reset type by " + string);
            }
        });
        id("reset type");
        requestMessage(LocaleEnum.EDIT_SETTING_RESET_TYPE.getWithPrefix());
        completeMessage(LocaleEnum.EDIT_SETTINGS_RESET_TYPE_SUCCESS.getWithPrefix());
        onComplete(type -> {
            settings.setResetType(type);
            settings.getMine().save(true);
        });
    }
}
