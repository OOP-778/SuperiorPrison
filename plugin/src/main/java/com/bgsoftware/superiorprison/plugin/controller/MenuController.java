package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.constant.MenuNames;
import com.bgsoftware.superiorprison.plugin.menu.edit.EditMenu;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.menu.config.ConfigMenuTemplate;
import com.oop.orangeengine.menu.config.MenuTemplatesController;
import com.oop.orangeengine.yaml.OConfiguration;
import lombok.Getter;

@Getter
public class MenuController extends MenuTemplatesController {

    private EditMenu editMenu;
    public MenuController(OConfiguration configuration) {
        super(configuration);

        OptionalConsumer<ConfigMenuTemplate> editMineMenu = findTemplateById(MenuNames.MINE_EDIT.getId());
        if (editMineMenu.isPresent())
            this.editMenu = new EditMenu(editMineMenu.get());

    }

}
