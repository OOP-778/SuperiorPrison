package com.bgsoftware.superiorprison.plugin.menu.settings.impl;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.settings.SettingsObject;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SResetSettings;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;

public class ResetValueSetting extends SettingsObject<Long> {
    public ResetValueSetting(SMineSettings settings) {
        super(Long.class, settings.getResetSettings().getValue());
        id(settings.getResetSettings().isTimed() ? "interval" : "percentage");

        if (settings.getResetSettings().isTimed())
            mapper(TimeUtil::toSeconds);

        else
            mapper(string -> {
                try {
                    return Long.parseLong(string);
                } catch (Throwable throwable) {
                    throw new IllegalStateException("Invalid number: " + string);
                }
            });

        requestMessage(LocaleEnum.EDIT_SETTINGS_VALUE.getWithPrefix());
        completeMessage(LocaleEnum.EDIT_SETTINGS_VALUE_SUCCESS.getWithPrefix());
        onComplete(value -> {
            settings.getResetSettings().setValue(value);
            settings.getMine().save(true);
            if (settings.getResetSettings().isTimed()) {
                SResetSettings.STimed timed = (SResetSettings.STimed) settings.getResetSettings().asTimed();
                timed.setResetDate(null);
            }
            settings.getMine().getLinker().call(settings);
        });
    }
}
