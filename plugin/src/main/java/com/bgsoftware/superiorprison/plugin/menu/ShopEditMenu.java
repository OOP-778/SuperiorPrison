package com.bgsoftware.superiorprison.plugin.menu;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.shop.SShopItem;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.bgsoftware.superiorprison.plugin.util.input.Input;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.oop.orangeengine.material.OMaterial;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@Getter
public class ShopEditMenu extends OPagedMenu<SShopItem> implements OMenu.Templateable {

  private final SNormalMine mine;

  public ShopEditMenu(SPrisoner viewer, SNormalMine mine) {
    super("shopEdit", viewer);
    this.mine = mine;

    ClickHandler.of("shop item")
        .handle(
            event -> {
              SShopItem shopItem = requestObject(event.getRawSlot());
              if (event.getClick().name().contains("RIGHT")) {
                getMine().getShop().removeItem(shopItem);
                refreshMenus(
                    ShopEditMenu.class,
                    menu -> menu.getMine().getName().contentEquals(getMine().getName()));

              } else {
                forceClose();
                LocaleEnum.EDIT_SHOP_WRITE_PRICE.getWithPrefix().send(event.getWhoClicked());

                Runnable onCancel = this::refresh;
                Input.bigDecimalInput(event.getWhoClicked())
                    .onCancel(onCancel)
                    .timeOut(TimeUnit.MINUTES, 2)
                    .onInput(
                        (obj, input) -> {
                          shopItem.setPrice(input);
                          mine.save(true);

                          LocaleEnum.EDIT_SHOP_PRICE_SET
                              .getWithPrefix()
                              .send(
                                  ImmutableMap.of(
                                      "{item_name}",
                                      TextUtil.beautifyName(shopItem.getItem()),
                                      "{item_price}",
                                      shopItem.getPrice().toString()),
                                  event.getWhoClicked());
                          refreshMenus(
                              ShopEditMenu.class,
                              menu -> menu.getMine().getName().contentEquals(mine.getName()));
                          obj.cancel();
                        })
                    .listen();
              }
            })
        .apply(this);
  }

  @Override
  public List<SShopItem> requestObjects() {
    return new ArrayList<>(mine.getShop().getItems());
  }

  @Override
  public OMenuButton toButton(SShopItem obj) {
    Optional<OMenuButton> item = getTemplateButtonFromTemplate("shop item");
    if (!item.isPresent()) return null;

    OMenuButton button = item.get().clone();
    OMenuButton.ButtonItemBuilder clone = button.getDefaultStateItem().clone();
    clone.itemBuilder().setMaterial(OMaterial.matchMaterial(obj.getItem()));

    if (obj.getItem().hasItemMeta()) {
      if (obj.getItem().getItemMeta().hasDisplayName())
        clone.itemBuilder().setDisplayName(obj.getItem().getItemMeta().getDisplayName());

      if (obj.getItem().getItemMeta().hasLore()) {
        clone.itemBuilder().setLore(obj.getItem().getItemMeta().getLore());
        clone.itemBuilder().mergeLore(button.getDefaultStateItem().getItemStack());
      }
    }

    button.currentItem(clone.getItemStackWithPlaceholdersMulti(getViewer(), mine, obj));
    return button;
  }

  @Override
  public void handleBottomClick(InventoryClickEvent event) {
    ItemStack clone = event.getCurrentItem().clone();
    clone.setAmount(1);
    event.setCancelled(true);

    if (mine.getShop().hasItem(clone)) {
      messageBuilder(LocaleEnum.SHOP_EDIT_ALREADY_HAS_ITEM.getWithErrorPrefix())
          .replace(mine, mine.getShop())
          .send(event.getWhoClicked());
      return;
    }

    mine.getShop().addItem(clone, new BigDecimal(0));
    mine.save(true);
    refreshMenus(
        ShopEditMenu.class, menu -> menu.getMine().getName().contentEquals(mine.getName()));
  }

  @Override
  public OMenu getMenu() {
    return this;
  }

  @Override
  public Object[] getBuildPlaceholders() {
    return Lists.newArrayList(getViewer(), mine).toArray();
  }
}
