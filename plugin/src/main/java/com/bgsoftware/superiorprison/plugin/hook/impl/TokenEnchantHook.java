package com.bgsoftware.superiorprison.plugin.hook.impl;

import com.bgsoftware.superiorprison.plugin.hook.SHook;
import com.oop.orangeengine.main.events.SyncEvents;
import com.vk2gpz.tokenenchant.event.TEBlockExplodeEvent;

public class TokenEnchantHook extends SHook {
    public TokenEnchantHook() {
        super(null);
    }

    @Override
    public String getPluginName() {
        return "TokenEnchant";
    }
}
