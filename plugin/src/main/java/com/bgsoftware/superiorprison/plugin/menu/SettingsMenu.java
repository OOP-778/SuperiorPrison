package com.bgsoftware.superiorprison.plugin.menu;

import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;

public class SettingsMenu extends OMenu implements OMenu.Placeholderable {

    public SettingsMenu(SNormalMine mine, SPrisoner viewer) {
        super("settings", viewer);
    }

    @Override
    public OMenu getMenu() {
        return this;
    }
}
