package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.oop.orangeengine.main.task.OTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PlayerUpdateTask extends OTask {

    private PlayerUpdateTask instance;

    private Cache<Player, Location> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .build();

    public PlayerUpdateTask() {
        this.instance = this;

        delay(TimeUnit.MILLISECONDS, 500);
        repeat(true);

        runnable(() -> {

            Set<String> worldNames = SuperiorPrisonPlugin.getInstance().getMineController().getMinesWorlds();
            for (Player player : cache.asMap().keySet()) {
                if (!worldNames.contains(player.getWorld().getName()))
                    cache.invalidate(player);

            }

            for (Player player : Collections.unmodifiableCollection(Bukkit.getOnlinePlayers())) {
                if (!worldNames.contains(player.getWorld().getName())) continue;

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
