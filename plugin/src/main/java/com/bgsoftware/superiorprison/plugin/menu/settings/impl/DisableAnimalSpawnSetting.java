package com.bgsoftware.superiorprison.plugin.menu.settings.impl;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.settings.SettingsObject;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;

public class DisableAnimalSpawnSetting extends SettingsObject<Boolean> {
  public DisableAnimalSpawnSetting(SMineSettings settings) {
    super(Boolean.class, settings.isDisableAnimalSpawn());
    completeMessage(LocaleEnum.EDIT_SETTINGS_VALUE_SUCCESS.getWithPrefix());
    onComplete(
        disable -> {
          settings.setDisableAnimalSpawn(disable);
          settings.getMine().getLinker().call(settings);
          settings.getMine().save(true);
        });
    id("disable animal spawn");
  }
}
