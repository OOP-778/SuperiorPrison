package com.bgsoftware.superiorprison.plugin.newMenu;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                        Optional<SPLocation> spawnPoint = mine.getSpawnPoint();
                        if (spawnPoint.isPresent())
                            event.getWhoClicked().teleport(spawnPoint.get().toBukkit());

                        else
                            LocaleEnum.MINE_TELEPORT_FAILED_SPAWN_NOT_SET.getWithErrorPrefix().send((Player) event.getWhoClicked());

                    } else if (event.getClick().name().contains("SHIFT") && event.getWhoClicked().hasPermission("superiorprison.admin"))
                        new MineControlPanel(getViewer(), requestObject(event.getRawSlot())).open(this);
                })
                .apply(this);
    }

    @Override
    public List<SNormalMine> requestObjects() {
        return SuperiorPrisonPlugin.getInstance().getDataController().getMines()
                .stream()
                //.filter(mine -> getViewer().getPlayer().isOp() || getViewer().getPlayer().hasPermission(mine.getPermission().orElse("")))
                .sorted(Comparator.comparing(SuperiorMine::getName))
                .map(mine -> (SNormalMine) mine)
                .collect(Collectors.toList());
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
