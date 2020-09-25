package com.bgsoftware.superiorprison.plugin.hook.impl;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.SHook;
import com.bgsoftware.superiorprison.plugin.util.placeholders.PlaceholderParser;
import com.bgsoftware.superiorprison.plugin.util.placeholders.parser.ObjectCache;

public class MVDWPapi extends SHook {
    public MVDWPapi() {
        super(null);
        PlaceholderAPI.registerPlaceholder(SuperiorPrisonPlugin.getInstance(), "prison_*", e -> {
            ObjectCache cache = new ObjectCache();
            SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(e.getViewingPlayer().getUniqueId()).ifPresent(cache::add);
            return PlaceholderParser.parse(e.getPlaceholder().replace("prison_", "").split("_"), cache);
        });
    }

    @Override
    public String getPluginName() {
        return "MVdWPlaceholderAPI";
    }
}
