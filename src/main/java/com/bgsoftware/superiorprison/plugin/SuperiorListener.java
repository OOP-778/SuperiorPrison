package com.bgsoftware.superiorprison.plugin;

import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.player.OPlayer;
import com.oop.orangeengine.main.player.PlayerController;
import org.bukkit.ChatColor;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.concurrent.TimeUnit;

import static com.oop.orangeengine.main.Engine.getEngine;
import static com.oop.orangeengine.main.Helper.color;

public class SuperiorListener {

    public SuperiorListener() {
        SuperiorPrisonPlugin plugin = SuperiorPrisonPlugin.getInstance();

        // <<< TESTING >>>
        SyncEvents.listen(AsyncPlayerChatEvent.class, event -> {
            if (event.getMessage().startsWith("startMine")) {

                // Create a "form" for a player
                event.getPlayer().sendMessage(color("&cPlease provide mine name to continue!"));

                OPlayer oPlayer = getEngine().findComponentByClass(PlayerController.class).lookupInsert(event.getPlayer().getUniqueId());
                oPlayer.setAllowedToReceive(false);

                SubscriptionFactory sf = SubscriptionFactory.getInstance();
                sf.subscribeTo(AsyncPlayerChatEvent.class, chatEvent2 -> {
                    final String prisonName = ChatColor.stripColor(chatEvent2.getMessage());



                }, new SubscriptionProperties<AsyncPlayerChatEvent>().timeOut(TimeUnit.SECONDS, 5).timesToRun(1).onTimeOut((se) -> event.getPlayer().sendMessage(color("&cFailed to provide prison name!"))));

            }

        });

    }

}
