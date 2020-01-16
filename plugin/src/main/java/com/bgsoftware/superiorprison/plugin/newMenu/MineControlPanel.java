package com.bgsoftware.superiorprison.plugin.newMenu;

import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.concurrent.TimeUnit;

@Getter
public class MineControlPanel extends OMenu {

    private SNormalMine mine;

    public MineControlPanel(SPrisoner viewer, SNormalMine mine) {
        super("mineControlPanel", viewer);
        this.mine = mine;

        // Action handler for set permission
        ClickHandler
                .of("set permission")
                .handle(event -> {
                    event.getWhoClicked().closeInventory();

                    //TODO: Configurable
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Write new permission!");

                    SubscriptionFactory.getInstance().subscribeTo(
                            AsyncPlayerChatEvent.class,
                            chatEvent -> {
                                mine.setPermission(chatEvent.getMessage());
                                chatEvent.setCancelled(true);
                                chatEvent.getPlayer().sendMessage(ChatColor.RED + "Mine permission has been set to " + chatEvent.getMessage());

                                // Update button
                                event.getWhoClicked().openInventory(event.getInventory());
                                refreshMenus(getClass(), menu -> menu.getMine().getName().contentEquals(getMine().getName()));
                                mine.save(true);
                            },
                            new SubscriptionProperties<AsyncPlayerChatEvent>()
                                    .timesToRun(1)
                                    .timeOut(TimeUnit.SECONDS, 30)
                    );
                });

        ClickHandler
                .of("edit generator")
                .handle(event -> {
                    previousMove = false;
                    new GeneratorEditMenu(getViewer(), getMine()).open(this);
                })
                .apply(this);
    }
}
