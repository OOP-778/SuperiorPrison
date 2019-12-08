package com.bgsoftware.superiorprison.plugin.menu.edit;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.enums.MenuNames;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.config.ConfigMenuTemplate;
import com.oop.orangeengine.menu.config.action.ActionListenerController;
import com.oop.orangeengine.menu.config.action.ActionProperties;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class EditMenu extends EditMenuHelper {

    private ConfigMenuTemplate template;
    private GeneratorMenu generatorMenu;

    public EditMenu(ConfigMenuTemplate template) {
        this.template = template;
        this.generatorMenu = new GeneratorMenu(template.getChildren().get(MenuNames.MINE_EDIT_GENERATOR.getId()));

        final String menuId = MenuNames.MINE_EDIT.getId();
        ActionListenerController.getInstance().listen(
                new ActionProperties<>(ButtonClickEvent.class)
                .actionId("edit permission")
                .menuId(menuId)
                .buttonAction(event -> {
                    event.getPlayer().closeInventory();

                    //TODO: Configurable
                    event.getPlayer().sendMessage(ChatColor.RED + "Write new permission!");

                    SubscriptionFactory.getInstance().subscribeTo(
                            AsyncPlayerChatEvent.class,
                            chatEvent -> {
                                SNormalMine mine = event.getMenu().grab("mine", SNormalMine.class).get();
                                mine.setPermission(chatEvent.getMessage());
                                chatEvent.setCancelled(true);
                                chatEvent.getPlayer().sendMessage(ChatColor.RED + "Mine permission has been set to " + chatEvent.getMessage());

                                // Update button
                                updateButton(event.getClickedButton(), mine);

                                event.getWrappedInventory().open(event.getPlayer());
                                SuperiorPrisonPlugin.getInstance().getDataController().save(mine, true);
                                },
                            new SubscriptionProperties<AsyncPlayerChatEvent>()
                            .timesToRun(1)
                            .timeOut(TimeUnit.SECONDS, 30)
                    );
                })
        );
    }

    public AMenu build(SNormalMine mine) {
        AMenu menu = template.build(false);
        menu.title(menu.title().replace("%mine_name%", mine.getName()));

        menu.addChild(generatorMenu.build(mine));
        parseButtons(menu, mine);

        menu.store("mine", mine);
        menu.getAllChildren().forEach(children -> children.store("mine", mine));
        return menu;
    }
}
