package com.bgsoftware.superiorprison.plugin.menu.edit;

import com.bgsoftware.superiorprison.plugin.enums.MenuNames;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.config.ConfigMenuTemplate;
import org.apache.commons.lang3.StringUtils;

public class GeneratorMenu extends EditMenuHelper {

    private ConfigMenuTemplate template;

    public GeneratorMenu(ConfigMenuTemplate menuTemplate) {
        this.template = menuTemplate;

        String menuId = MenuNames.MINE_EDIT_GENERATOR.getId();
    }

    @Override
    public AMenu build(SNormalMine mine) {
        AMenu menu = template.build();
        menu.title(menu.title().replace("%mine_name%", mine.getName()));
        parseButtons(menu, mine);

        fillMaterials(menu, mine);

        menu.store("mine", mine);
        menu.getAllChildren().forEach(children -> children.store("mine", mine));

        return menu;
    }

    public void fillMaterials(AMenu menu, SNormalMine mine) {
        AMenuButton templateButton = menu.buttons().stream()
                .filter(button -> button.containsData("template") && button.grab("template", String.class).get().contentEquals("materialTemplate"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to find material template for Generator Edit menu"));

        // Remove old buttons with mats
        menu.removeButtonIfMatched(button -> button.containsData("mineMaterial"));

        // Fill new materials
        mine.getGenerator().getGeneratorMaterials().forEach(pair -> {
            AMenuButton button = templateButton.clone();
            OItem buttonItem = new OItem(button.currentItem().clone());

            buttonItem.replaceDisplayName("%material_name%", beautify(pair.getSecond().name()));
            buttonItem.replaceInLore("%material_rate%", pair.getFirst() + "");

            button.currentItem(buttonItem.getItemStack());
            button.store("mineMaterial", pair.getSecond());

            menu.addButton(button.paged(true));
        });
    }

    public String beautify(String text) {
        return StringUtils.capitalize(text.toLowerCase().replace("_", " "));
    }
}
