package com.bgsoftware.superiorprison.plugin.menu;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.menu.control.MineControlPanel;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.oop.orangeengine.item.custom.OItem;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class MinesListMenu extends OPagedMenu<SNormalMine> implements OMenu.Templateable {
  public MinesListMenu(SPrisoner viewer) {
    super("minesList", viewer);

    ClickHandler.of("mine click")
        .handle(
            event -> {
              if (event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.LEFT) {
                SNormalMine mine = requestObject(event.getRawSlot());
                Framework.FRAMEWORK.teleport((Player) event.getWhoClicked(), mine.getSpawnPoint());

              } else if (event.getClick().name().contains("SHIFT")
                  && event.getWhoClicked().hasPermission("prison.admin.editmine")) {
                move(new MineControlPanel(getViewer(), requestObject(event.getRawSlot())));
              }
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
    OMenuButton.ButtonItemBuilder parsedItem = buttonTemplate.getDefaultStateItem().clone();

    if (obj.getIcon().hasItemMeta()) {
      if (obj.getIcon().getItemMeta().hasDisplayName())
        parsedItem.itemBuilder().setDisplayName(obj.getIcon().getItemMeta().getDisplayName());

      if (obj.getIcon().getItemMeta().hasLore()) {
        parsedItem.itemBuilder().setLore(obj.getIcon().getItemMeta().getLore());
        parsedItem.itemBuilder().mergeLore(buttonTemplate.getDefaultStateItem().getItemStack());
      }
    }

    OItem mineIcon = new OItem(obj.getIcon().clone());
    mineIcon.setLore(parsedItem.itemBuilder().getLore());
    mineIcon.setDisplayName(parsedItem.itemBuilder().getDisplayName());

    buttonTemplate.currentItem(
        new OMenuButton.ButtonItemBuilder(mineIcon)
            .getItemStackWithPlaceholdersMulti(getViewer(), obj));
    return buttonTemplate;
  }

  @Override
  public OMenu getMenu() {
    return this;
  }
}
