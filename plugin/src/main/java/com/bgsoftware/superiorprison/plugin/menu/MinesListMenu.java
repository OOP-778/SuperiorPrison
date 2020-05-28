package com.bgsoftware.superiorprison.plugin.menu;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.menu.control.MineControlPanel;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class MinesListMenu extends OPagedMenu<SNormalMine> implements OMenu.Templateable {
    public MinesListMenu(SPrisoner viewer) {
        super("minesList", viewer);

        ClickHandler
                .of("mine click")
                .handle(event -> {
                    // If normal click
                    if (event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.LEFT) {
                        //TODO: Add teleport timer and move check
                        SNormalMine mine = requestObject(event.getRawSlot());
                        event.getWhoClicked().teleport(mine.getSpawnPoint());

                    } else if (event.getClick().name().contains("SHIFT") && event.getWhoClicked().hasPermission("superiorprison.admin"))
                        move(new MineControlPanel(getViewer(), requestObject(event.getRawSlot())));
                })
                .apply(this);
    }

    @Override
    public List<SNormalMine> requestObjects() {
        return SuperiorPrisonPlugin.getInstance().getMineController().getMinesFor(getViewer());
    }

    @Override
    public OMenuButton toButton(SNormalMine obj) {
        OMenuButton buttonTemplate = getTemplateButtonFromTemplate("mine template").orElse(null);
        if (buttonTemplate == null) return null;

        buttonTemplate = buttonTemplate.clone();
        OMenuButton.ButtonItemBuilder parsedItem = buttonTemplate
                .getDefaultStateItem().clone();

        if (obj.getIcon().hasItemMeta()) {
            if (obj.getIcon().getItemMeta().hasDisplayName())
                parsedItem.itemBuilder().setDisplayName(obj.getIcon().getItemMeta().getDisplayName());

            if (obj.getIcon().getItemMeta().hasLore()) {
                parsedItem.itemBuilder().setLore(obj.getIcon().getItemMeta().getLore());
                parsedItem.itemBuilder().mergeLore(buttonTemplate.getDefaultStateItem().getItemStack());
            }
        }

        parsedItem.itemBuilder().setMaterial(OMaterial.matchMaterial(obj.getIcon()));

        buttonTemplate.currentItem(parsedItem.getItemStackWithPlaceholdersMulti(getViewer(), obj));
        return buttonTemplate;
    }

    @Override
    public OMenu getMenu() {
        return this;
    }
}
