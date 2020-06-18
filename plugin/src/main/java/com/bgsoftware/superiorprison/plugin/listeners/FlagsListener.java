package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.flags.Flag;
import com.bgsoftware.superiorprison.api.event.mine.MineEnterEvent;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

public class FlagsListener {

    private final SMineHolder mineHolder;
    private final SPrisonerHolder prisonerHolder;

    public FlagsListener() {
        SuperiorPrisonPlugin plugin = SuperiorPrisonPlugin.getInstance();
        mineHolder = plugin.getMineController();
        prisonerHolder = plugin.getPrisonerController();

        // PvP flag
        SyncEvents.listen(EntityDamageByEntityEvent.class, event -> {
            if (event.getDamager().getType() != EntityType.PLAYER) return;
            if (event.getEntity().getType() != EntityType.PLAYER) return;

            check((Player) event.getDamager(), Flag.PVP, event.getDamager().getLocation(), event);
        });

        // Build flag
        SyncEvents.listen(BlockPlaceEvent.class, EventPriority.HIGHEST, event -> {
            Optional<SuperiorMine> mineAt = mineHolder.getMineAt(event.getBlock().getLocation());
            if (!mineAt.isPresent())
                return;

            if (!hasBypass(event.getPlayer()))
                event.setCancelled(true);
        });

        // Fall flag
        SyncEvents.listen(EntityDamageEvent.class, event -> {
            if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
            if (event.getEntityType() != EntityType.PLAYER) return;

            check((Player) event.getEntity(), Flag.FALL_DAMAGE, event.getEntity().getLocation(), event);
        });

        // Hunger flag
        SyncEvents.listen(FoodLevelChangeEvent.class, event -> check((Player) event.getEntity(), Flag.HUNGER, event.getEntity().getLocation(), event));
    }

    public void check(Player player, Flag flag, Location location, Cancellable event) {
        Optional<SuperiorMine> mineAt = mineHolder.getMineAt(location);
        if (!mineAt.isPresent())
            return;

        SuperiorMine iSuperiorMine = mineAt.get();
        Area area = iSuperiorMine.getArea(location);
        if (!area.getFlagState(flag) && !hasBypass(player))
            event.setCancelled(true);
    }

    public boolean hasBypass(Player player) {
        return player.hasPermission("superiorprison.flags.bypass");
    }

}
