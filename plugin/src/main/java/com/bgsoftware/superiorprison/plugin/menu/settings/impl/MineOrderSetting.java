package com.bgsoftware.superiorprison.plugin.menu.settings.impl;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.settings.SettingsObject;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;

public class MineOrderSetting extends SettingsObject<Integer> {
    public MineOrderSetting(SMineSettings settings) {
        super(Integer.class, settings.getOrder());
        mapper(string -> {
            try {
                return Integer.parseInt(string);
            } catch (Throwable throwable) {
                throw new IllegalStateException("Invalid int: " + string);
            }
        });
        requestMessage(LocaleEnum.EDIT_SETTINGS_ORDER.getWithPrefix());
        completeMessage(LocaleEnum.EDIT_SETTINGS_ORDER_SUCCESS.getWithPrefix());
        onComplete(order -> {
            settings.setOrder(order);
            settings.getMine().save(true);
        });
        id("mine order");
    }
}
