package com.bgsoftware.superiorprison.plugin.menu.edit;

import com.bgsoftware.superiorprison.api.data.mine.type.NormalMine;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.config.ConfigMenuTemplate;
import com.oop.orangeengine.menu.config.action.ActionListenerController;
import com.oop.orangeengine.menu.config.action.ActionProperties;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class EditMenu {

    public ConfigMenuTemplate template;
    public EditMenu(ConfigMenuTemplate template) {
        this.template = template;

        ActionListenerController.getInstance().listen(
                new ActionProperties<ButtonClickEvent>(ButtonClickEvent.class)
                .actionId("edit permission")
                .buttonAction(event -> {
                    event.getPlayer().closeInventory();

                    //TODO: Configurable
                    event.getPlayer().sendMessage(ChatColor.RED + "Write new permission!");

                    SubscriptionFactory.getInstance().subscribeTo(
                            AsyncPlayerChatEvent.class,
                            chatEvent -> {
                                SNormalMine mine = (SNormalMine) event.getMenu().grab("mine").get();
                                mine.setPermission(chatEvent.getMessage());
                                chatEvent.getPlayer().sendMessage(ChatColor.RED + "Mine permission has been set to " + chatEvent.getMessage());

                                // Update button
                                ItemStack parsed = parsePlaceholders(event.getClickedButton().grab("placeholder", ItemStack.class).get(), mine);
                                event.getClickedButton().currentItem(parsed);

                                event.getWrappedInventory().open(event.getPlayer());
                            },
                            new SubscriptionProperties<AsyncPlayerChatEvent>()
                            .timesToRun(1)
                            .timeOut(TimeUnit.SECONDS, 30)
                    );
                })
        );

    }

    public AMenu build(SNormalMine mine) {
        AMenu menu = template.build();

        // Parse placeholders
        menu.buttons().forEach(button -> {
            button.saveCurrentItem("placeholder");
            button.currentItem(parsePlaceholders(button.currentItem(), mine));
        });

        menu.store("mine", mine);
        menu.getAllChildren().forEach(children -> children.store("mine", mine));
        return menu;
    }

    public ItemStack parsePlaceholders(ItemStack itemStack, SNormalMine mine) {
        OItem item = new OItem(itemStack);

        // Parse display name
        item.setDisplayName(SuperiorPrisonPlugin.getInstance().getPlaceholderController().parse(item.getDisplayName(),  mine));

        // Parse lore
        item.setLore(SuperiorPrisonPlugin.getInstance().getPlaceholderController().parse(item.getLore(), mine));

        return item.getItemStack();
    }

}
