package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.data.map.OMap;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerUpdateTask extends OTask {

   private Cache<Player, Location> cache = CacheBuilder.newBuilder()
           .expireAfterAccess(10, TimeUnit.SECONDS)
           .build();

    public PlayerUpdateTask() {
        delay(TimeUnit.MILLISECONDS, 500);
        repeat(true);

        runnable(() -> {

            Set<String> worldNames = SuperiorPrisonPlugin.getInstance().getMineController().getMinesWorlds();
            for (Player player : cache.asMap().keySet()) {
                if (!worldNames.contains(player.getWorld().getName()))
                    cache.invalidate(player);

            }

            for (Player player : Collections.unmodifiableCollection(Bukkit.getOnlinePlayers())) {
                Location lastLocation = cache.getIfPresent(player);
                if (lastLocation != null) {
                    Location currentLocation = player.getLocation();
                    if (currentLocation.distanceSquared(lastLocation) < 1) continue;

                }
            }
        });

        execute();

    }

}
