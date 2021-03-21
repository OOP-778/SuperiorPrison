package com.bgsoftware.superiorprison.plugin.menu.backpack;

import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.inventory.PatchedInventory;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;

import java.math.BigInteger;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SimpleBackPackView extends OMenu implements BackpackLockable {

  private final SBackPack backPack;

  public SimpleBackPackView(SPrisoner viewer, SBackPack backPack) {
    super("simpleBackPackView", viewer);
    this.backPack = backPack;

    clickHandler("upgrade").handle(event -> move(new BackPackUpgradeMenu(viewer, backPack)));

    clickHandler("64 withdraw")
        .handle(
            event -> {

            });

    clickHandler("full withdraw")
        .handle(
            event -> {

            });

    clickHandler("sellContentsFlag")
        .handle(
            event -> {
              backPack.getData().setSell(!backPack.getData().isSell());
              refresh();
            });

    getStateRequester()
        .registerRequest(
            "sellContentsFlag", button -> getToggleableState(button, backPack.getData().isSell()));
  }

  public void doWithdraw(BigInteger amount) {

  }

  @Override
  public void closeInventory(InventoryCloseEvent event) {
    super.closeInventory(event);

    // If action is null, save the inventory page
    if (getCurrentAction() != null) return;

    updateBackpackAndUnlock();
  }

  @Override
  public Object[] getBuildPlaceholders() {
    return new Object[] {getViewer(), backPack};
  }

  private OMenuButton.ButtonItemBuilder getToggleableState(OMenuButton button, boolean state) {
    if (state) return button.getStateItem("enabled");
    else return button.getStateItem("disabled");
  }
}
