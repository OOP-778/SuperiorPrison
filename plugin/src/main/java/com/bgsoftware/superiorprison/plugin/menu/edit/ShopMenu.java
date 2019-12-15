package com.bgsoftware.superiorprison.plugin.menu.edit;

import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.config.ConfigMenuTemplate;
import com.oop.orangeengine.menu.config.action.ActionListenerController;
import com.oop.orangeengine.menu.config.action.ActionProperties;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
                            clickEvent.getPlayer().sendMessage("Write new title for shop!");
                            clickEvent.getPlayer().closeInventory();

                            SubscriptionFactory.getInstance().subscribeTo(
                                    AsyncPlayerChatEvent.class,
                                    chatEvent -> {
                                        chatEvent.setCancelled(true);
                                        SNormalMine mine = clickEvent.getMenu().grab("mine", SNormalMine.class).get();
                                        mine.getShop().setTitle(chatEvent.getMessage());

                                        chatEvent.getPlayer().sendMessage("Shop title was set to: " + chatEvent.getMessage());
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
    }

    @Override
    public AMenu build(SNormalMine mine) {
        AMenu menu = configMenuTemplate.build();
        menu.title(menu.title().replace("%mine_name%", mine.getName()));

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
            OItem buttonItem = new OItem(shopItem.getItem());

            buttonItem.setLore(lore);
            buttonItem.setDisplayName(displayName.replace("%item_name%", shopItem.getItem().getItemMeta().hasDisplayName() ? shopItem.getItem().getItemMeta().getDisplayName() : beautify(shopItem.getItem().getType().name())));

            buttonItem.replaceInLore("%item_price%", "" + shopItem.getPrice());

            button.currentItem(buttonItem.getItemStack());
            button.store("shopItem", shopItem.getItem());

            menu.addButton(button.paged(true));
        });
    }


}
