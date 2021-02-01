package com.bgsoftware.superiorprison.plugin.menu.backpack;

import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.inventory.PatchedInventory;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
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
              int canAdd = 64;
              for (int i = 0; i < backPack.getData().getStored().length; i++) {
                ItemStack itemStack = backPack.getData().getStored()[i];
                if (itemStack == null) continue;

                ItemStack itemClone = itemStack.clone();
                if (itemClone.getAmount() > canAdd) itemClone.setAmount(canAdd);

                ((PatchedInventory) event.getWhoClicked().getInventory()).setOwnerCalling();
                HashMap<Integer, ItemStack> left =
                    event.getWhoClicked().getInventory().addItem(itemClone);
                if (left.isEmpty()) {
                  backPack.getData().getStored()[i] = null;
                  canAdd -= itemClone.getAmount();

                } else {
                  ItemStack leftItem = left.values().stream().findFirst().orElse(null);
                  itemStack.setAmount(itemStack.getAmount() - leftItem.getAmount());
                  canAdd -= leftItem.getAmount();
                  if (itemStack.getAmount() != 0) backPack.getData().getStored()[i] = leftItem;
                  else backPack.getData().getStored()[i] = null;
                }
                break;
              }
            });

    clickHandler("full withdraw")
        .handle(
            event -> {
              PlayerInventory inventory = event.getWhoClicked().getInventory();
              for (int i = 0; i < backPack.getData().getStored().length; i++) {
                ItemStack itemStack = backPack.getData().getStored()[i];
                if (itemStack == null || itemStack.getType() == Material.AIR) continue;

                ((PatchedInventory) event.getWhoClicked().getInventory()).setOwnerCalling();
                HashMap<Integer, ItemStack> left = inventory.addItem(itemStack);
                backPack.getData().getStored()[i] =
                    left.isEmpty() ? null : left.values().toArray(new ItemStack[0])[0];
              }
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
