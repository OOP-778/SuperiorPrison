package com.bgsoftware.superiorprison.plugin.menus;

import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;

public class MenuEditMine extends OMenu {

    private SNormalMine mine;

    public MenuEditMine(SPrisoner viewer, SNormalMine mine) {
        super("menuEditMine", viewer);
        ClickHandler
                .of("remove mine")
                .handle(event -> {
                })
                .apply(this);
    }
}
