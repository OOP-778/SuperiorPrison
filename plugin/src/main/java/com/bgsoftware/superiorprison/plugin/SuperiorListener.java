package com.bgsoftware.superiorprison.plugin;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.flags.FlagEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.player.OPlayer;
import com.oop.orangeengine.main.player.PlayerController;
import com.oop.orangeengine.main.util.OptionalConsumer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.concurrent.TimeUnit;

import static com.oop.orangeengine.main.Engine.getEngine;
import static com.oop.orangeengine.main.Helper.color;

public class SuperiorListener {

    public SuperiorListener() {

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

                    event.getPlayer().sendMessage(color("&cNow click a block for pos1"));
                    sf.subscribeTo(PlayerInteractEvent.class, pos1Event -> {

                        final Location pos1 = pos1Event.getClickedBlock().getLocation();
                        event.getPlayer().sendMessage(color("&cNow click a block for pos2"));
                        sf.subscribeTo(PlayerInteractEvent.class, pos2Event -> {

                            final Location pos2 = pos2Event.getClickedBlock().getLocation();
                            event.getPlayer().sendMessage(color("&cSuccessfully created a mine"));
                            SNormalMine normalMine = new SNormalMine(prisonName, pos1, pos2);
                            event.getPlayer().sendMessage(color("Successfully created mine!"));

                        }, new SubscriptionProperties<PlayerInteractEvent>().timeOut(TimeUnit.SECONDS, 5).timesToRun(1).filter(filterEvent -> filterEvent.getClickedBlock() != null && filterEvent.getClickedBlock().getType() != Material.AIR).onTimeOut((se) -> event.getPlayer().sendMessage(color("&cFailed to provide pos 2 for mine!"))));
                    }, new SubscriptionProperties<PlayerInteractEvent>().timeOut(TimeUnit.SECONDS, 5).timesToRun(1).filter(filterEvent -> filterEvent.getClickedBlock() != null && filterEvent.getClickedBlock().getType() != Material.AIR).onTimeOut((se) -> event.getPlayer().sendMessage(color("&cFailed to provide pos 1 for mine!"))));
                }, new SubscriptionProperties<AsyncPlayerChatEvent>().timeOut(TimeUnit.SECONDS, 5).timesToRun(1).onTimeOut((se) -> event.getPlayer().sendMessage(color("&cFailed to provide prison name!"))));

            }
        });

        // Protection from PVP
        SyncEvents.listen(EntityDamageByEntityEvent.class, event -> {
            OptionalConsumer<SuperiorMine> mineAtLocation = SuperiorPrisonPlugin.getInstance().getMineController().getMineAt(event.getDamager().getLocation());
            if (!mineAtLocation.isPresent()) return;


            SuperiorMine iSuperiorMine = mineAtLocation.get();
            if (iSuperiorMine.isFlag(FlagEnum.PVP))
                event.setCancelled(true);

        });
    }

}
