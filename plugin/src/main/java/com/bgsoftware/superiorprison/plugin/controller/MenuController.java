package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.enums.MenuNames;
import com.bgsoftware.superiorprison.plugin.menu.edit.EditMenu;
import com.oop.orangeengine.menu.config.ConfigMenuTemplate;
import lombok.Getter;

import java.util.Optional;

@Getter
public class MenuController {

    private EditMenu editMenu;
    public MenuController() {

        ConfigController configController = SuperiorPrisonPlugin.getInstance().getConfigController();
        Optional<ConfigMenuTemplate> editMineMenu = configController.findMenuTemplate(MenuNames.MINE_EDIT.getId());
        if (editMineMenu.isPresent())
            this.editMenu = new EditMenu(editMineMenu.get());

    }

}
