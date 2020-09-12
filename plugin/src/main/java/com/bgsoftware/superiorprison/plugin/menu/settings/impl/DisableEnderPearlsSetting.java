package com.bgsoftware.superiorprison.plugin.menu.settings.impl;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.settings.SettingsObject;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;

public class DisableEnderPearlsSetting extends SettingsObject<Boolean> {
    public DisableEnderPearlsSetting(SMineSettings settings) {
        super(Boolean.class, settings.isDisableEnderPearls());
        completeMessage(LocaleEnum.EDIT_SETTINGS_VALUE_SUCCESS.getWithPrefix());
        onComplete(disable -> {
            settings.setDisableEnderPearls(disable);
            settings.getMine().getLinker().call(settings);
            settings.getMine().save(true);
        });
        id("disable enderpearls");
    }
}