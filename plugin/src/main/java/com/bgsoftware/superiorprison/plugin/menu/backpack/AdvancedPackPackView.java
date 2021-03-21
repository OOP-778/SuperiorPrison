package com.bgsoftware.superiorprison.plugin.menu.backpack;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.backpack.BackPackItem;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.inventory.PatchedInventory;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.oop.orangeengine.item.custom.OItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.*;

public class AdvancedPackPackView extends OPagedMenu<Map.Entry<BackPackItem, BigInteger>>
    implements BackpackLockable, OMenu.Templateable {
  private final SBackPack backPack;

  public AdvancedPackPackView(SPrisoner viewer, SBackPack backPack) {
    super("advancedBackPackView", viewer);
    this.backPack = backPack;

    clickHandler("upgrade").handle(event -> move(new BackPackUpgradeMenu(viewer, backPack)));
    getStateRequester()
        .registerRequest(
            "sellContentsFlag", button -> getToggleableState(button, backPack.getData().isSell()));

    clickHandler("sellContentsFlag")
        .handle(
            event -> {
              backPack.getData().setSell(!backPack.getData().isSell());
              refresh();
            });

    clickHandler("item")
        .handle(
            event -> {
              BigInteger toRemove;
              Map.Entry<BackPackItem, BigInteger> entry = requestObject(event.getSlot());
              final Player player = (Player) event.getWhoClicked();

              if (event.isRightClick()) toRemove = entry.getValue();
              else toRemove = entry.getValue().min(BigInteger.valueOf(64));

              if (NumberUtil.isMoreThan(
                  toRemove,
                  BigInteger.valueOf(
                      SuperiorPrisonPlugin.getInstance()
                          .getMainConfig()
                          .getMaxWithdrawInBackpacks())))
                toRemove =
                    BigInteger.valueOf(
                        SuperiorPrisonPlugin.getInstance()
                            .getMainConfig()
                            .getMaxWithdrawInBackpacks());

              List<ItemStack> itemStacks = convertItem(entry.getKey().getItemStack(), toRemove);

              ((PatchedInventory) event.getWhoClicked().getInventory()).setOwnerCalling();
              HashMap<Integer, ItemStack> left =
                  player.getInventory().addItem(itemStacks.toArray(new ItemStack[0]));

              long sum = left.values().stream().mapToLong(ItemStack::getAmount).sum();
              BigInteger merge;

              if (sum == 0) {
                merge =
                    backPack
                        .getData()
                        .getItems()
                        .merge(entry.getKey(), toRemove, BigInteger::subtract);

              } else {
                BigInteger difference = toRemove.subtract(BigInteger.valueOf(sum));
                merge =
                    backPack
                        .getData()
                        .getItems()
                        .merge(entry.getKey(), difference, BigInteger::subtract);

                LocaleEnum.PRISONER_INVENTORY_FULL.getWithErrorPrefix().send(player);
              }

              if (NumberUtil.equals(merge, BigInteger.ZERO))
                backPack.getData().getItems().remove(entry.getKey());

              backPack.setUsed(backPack.getData().sumAllItems());
              refresh();
            });
  }

  public static List<ItemStack> convertItem(ItemStack itemStack, BigInteger amount) {
    List<ItemStack> items = new ArrayList<>();
    ItemStack clone;
    while (!NumberUtil.equals(amount, BigInteger.ZERO)) {
      clone = itemStack.clone();
      if (NumberUtil.isMoreThan(amount, BigInteger.valueOf(64))) {
        amount = amount.subtract(BigInteger.valueOf(64));
        clone.setAmount(64);

      } else {
        clone.setAmount(amount.intValue());
        amount = BigInteger.ZERO;
      }

      items.add(clone);
    }

    return items;
  }

  @Override
  public List<Map.Entry<BackPackItem, BigInteger>> requestObjects() {
    return new LinkedList<>(backPack.getData().getItems().entrySet());
  }

  @Override
  public OMenuButton toButton(Map.Entry<BackPackItem, BigInteger> obj) {
    try {
      return getTemplateButtonFromTemplate("item")
          .map(OMenuButton::clone)
          .map(
              itemTemplateButton -> {
                OMenuButton.ButtonItemBuilder template = itemTemplateButton.getDefaultStateItem();

                OItem item = new OItem(obj.getKey().getItemStack().clone());
                template
                    .itemBuilder()
                    .replace(
                        "{item_material}", TextUtil.beautify(obj.getKey().getMaterial().name()));
                template.itemBuilder().replace("{item_amount}", obj.getValue().toString());

                List<String> newLore = new LinkedList<>();
                for (String line : template.itemBuilder().getLore()) {
                  if (line.contains("{item_lore}")) {
                    newLore.addAll(item.getLore());
                    continue;
                  }

                  newLore.add(line);
                }

                item.setLore(newLore);
                item.setDisplayName(template.itemBuilder().getDisplayName());
                return itemTemplateButton.currentItem(item.getItemStack());
              })
          .orElse(null);
    } catch (Throwable throwable) {
      throwable.printStackTrace();
      return null;
    }
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
