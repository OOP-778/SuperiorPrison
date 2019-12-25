package com.bgsoftware.superiorprison.plugin.menu.edit;

import com.bgsoftware.superiorprison.api.data.mine.shop.ShopItem;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.shop.SShopItem;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.config.ConfigMenuTemplate;
import com.oop.orangeengine.menu.config.action.ActionListenerController;
import com.oop.orangeengine.menu.config.action.ActionProperties;
import com.oop.orangeengine.menu.config.button.types.ConfigNormalButton;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShopMenu extends EditMenuHelper {

    private ConfigMenuTemplate configMenuTemplate;

    protected ShopMenu(ConfigMenuTemplate configMenuTemplate) {
        this.configMenuTemplate = configMenuTemplate;

        ActionListenerController.getInstance().listen(
                new ActionProperties<>(ButtonClickEvent.class)
                        .menuId(configMenuTemplate.getMenuIdentifier())
                        .actionId("set title")
                        .buttonAction(clickEvent -> {
                            LocaleEnum.EDIT_SHOP_WRITE_NEW_TITLE.getWithPrefix().send(clickEvent.getPlayer());
                            clickEvent.getPlayer().closeInventory();

                            SubscriptionFactory.getInstance().subscribeTo(
                                    AsyncPlayerChatEvent.class,
                                    chatEvent -> {
                                        chatEvent.setCancelled(true);
                                        SNormalMine mine = clickEvent.getMenu().grab("mine", SNormalMine.class).get();
                                        mine.getShop().setTitle(chatEvent.getMessage());

                                        LocaleEnum.EDIT_SHOP_SET_TITLE.getWithPrefix().send(chatEvent.getPlayer(), ImmutableMap.of("%mine_shop_title%", mine.getShop().getTitle()));

                                        mine.save(true);
                                        updateButton(clickEvent.getClickedButton(), mine);
                                        clickEvent.getWrappedInventory().open(chatEvent.getPlayer());
                                    },
                                    new SubscriptionProperties<AsyncPlayerChatEvent>()
                                            .timesToRun(1)
                                            .timeOut(TimeUnit.MINUTES, 1)
                            );
                        })
        );
        ActionListenerController.getInstance().listen(
                new ActionProperties<>(ButtonClickEvent.class)
                        .menuId(configMenuTemplate.getMenuIdentifier())
                        .actionId("remove")
                        .buttonAction(clickEvent -> {
                            SShopItem item = clickEvent.getClickedButton().grab("shopItem", SShopItem.class).get();
                            SNormalMine mine = clickEvent.getMenu().grab("mine", SNormalMine.class).get();

                            mine.getShop().getItems().remove(item);
                            LocaleEnum.EDIT_SHOP_REMOVED_ITEM.getWithPrefix().send(clickEvent.getPlayer());

                            fillItems(clickEvent.getMenu(), mine);
                            clickEvent.getMenu().update();
                            SuperiorPrisonPlugin.getInstance().getDataController().save(mine, true);
                        })
        );

        ActionListenerController.getInstance().listen(
                new ActionProperties<>(ButtonClickEvent.class)
                        .menuId(configMenuTemplate.getMenuIdentifier())
                        .actionId("edit item")
                        .buttonAction(event -> {
                            SShopItem item = event.getClickedButton().grab("shopItem", SShopItem.class).get();

                            WrappedInventory wrappedInventory = generateItemEditMenu(item, event.getMenu());
                            wrappedInventory.open(event.getPlayer());
                        })
        );

    }

    @Override
    public AMenu build(SNormalMine mine) {
        AMenu menu = configMenuTemplate.build();
        menu.title(menu.title().replace("%mine_name%", mine.getName()));

        menu.bottomInvClickHandler(event -> {
            if (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) return;
            ItemStack clone = event.getCurrentItem().clone();

            mine.getShop().getItems().add(new SShopItem(clone, 0, 0));

            parseButtons(menu, mine);
            fillItems(menu, mine);
            menu.update();
        });

        parseButtons(menu, mine);
        fillItems(menu, mine);

        menu.store("mine", mine);
        menu.getAllChildren().forEach(children -> children.store("mine", mine));
        return menu;
    }

    public void fillItems(AMenu menu, SNormalMine mine) {
        AMenuButton templateButton = menu.buttons().stream()
                .filter(button -> button.containsData("template") && button.grab("template", String.class).get().contentEquals("itemTemplate"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to find material template for Shop Edit menu"));

        // Remove old buttons with mats
        menu.removeButtonIfMatched(button -> button.containsData("shopItem"));

        String displayName = templateButton.currentItem().getItemMeta().getDisplayName();
        List<String> lore = templateButton.currentItem().getItemMeta().getLore();

        // Fill new materials
        mine.getShop().getItems().forEach(shopItem -> {
            AMenuButton button = templateButton.clone();

            Helper.debug("Setting " + shopItem.getItem());
            OItem buttonItem = new OItem(shopItem.getItem().clone());

            buttonItem.setLore(lore);
            buttonItem.setDisplayName(displayName.replace("%item_name%", shopItem.getItem().getItemMeta().hasDisplayName() ? shopItem.getItem().getItemMeta().getDisplayName() : beautify(shopItem.getItem().getType().name())));

            buttonItem.replaceInLore("%item_buy_price%", "" + shopItem.getBuyPrice());
            buttonItem.replaceInLore("%item_sell_price%", "" + shopItem.getSellPrice());

            button.currentItem(buttonItem.getItemStack());
            button.store("shopItem", shopItem);

            menu.addButton(button.paged(true));
        });
    }

    public WrappedInventory generateItemEditMenu(ShopItem shopItem, AMenu menu) {
        AMenu itemEditMenu = menu.getChild("item edit menu", false).get();
        WrappedInventory wrappedInventory = itemEditMenu.getWrappedInventory();
        wrappedInventory.changeTitle(wrappedInventory.getTitle().replace("%item_name%", shopItem.getItem().getItemMeta().hasDisplayName() ? shopItem.getItem().getItemMeta().getDisplayName() : beautify(shopItem.getItem().getType().name())));

        OptionalConsumer<AMenuButton> setBuyPriceButton = wrappedInventory.findByFilter(button -> button.appliedActions().contains("set buy price"));
        OptionalConsumer<AMenuButton> setSellPriceButton = wrappedInventory.findByFilter(button -> button.appliedActions().contains("set sell price"));
        OptionalConsumer<AMenuButton> shopitemButton = wrappedInventory.findByFilter(button -> button.containsData("placeholder") && button.grab("placeholder", String.class).get().contentEquals("shop_item"));

        setBuyPriceButton.ifPresent(button -> updateButton(button, shopItem));
        setSellPriceButton.ifPresent(button -> updateButton(button, shopItem));
        shopitemButton.ifPresent(button -> {

            button.currentItem(shopItem.getItem(), false);
            updateButton(button, shopItem);

        });
        return wrappedInventory;
    }

}
