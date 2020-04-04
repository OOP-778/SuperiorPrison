package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.flags.Flag;
import com.bgsoftware.superiorprison.api.event.mine.MineEnterEvent;
import com.bgsoftware.superiorprison.api.event.mine.area.MineAreaChangeEvent;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.bgsoftware.superiorprison.plugin.util.XTitles;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

public class FlagsListener {

    private SMineHolder mineHolder;

    public FlagsListener() {
        SuperiorPrisonPlugin plugin = SuperiorPrisonPlugin.getInstance();
        mineHolder = plugin.getMineController();

        // PvP flag
        SyncEvents.listen(EntityDamageByEntityEvent.class, event -> check(Flag.PVP, event.getDamager().getLocation(), event));

        // Build flag
        SyncEvents.listen(BlockPlaceEvent.class, EventPriority.HIGHEST, event -> check(Flag.BUILD, event.getPlayer().getLocation(), event));

        // Break flag
        SyncEvents.listen(BlockBreakEvent.class, event -> check(Flag.BUILD, event.getPlayer().getLocation(), event));

        // Fall flag
        SyncEvents.listen(EntityDamageEvent.class, event -> {
            if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

            check(Flag.FALL_DAMAGE, event.getEntity().getLocation(), event);
        });

        // Hunger flag
        SyncEvents.listen(FoodLevelChangeEvent.class, event -> check(Flag.HUNGER, event.getEntity().getLocation(), event));

        // Night Vision flag
        SyncEvents.listen(MineEnterEvent.class, event -> {
            Area area = event.getArea();
            if (area.getFlagState(Flag.NIGHT_VISION))
                event.getPrisoner().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 9999, 1, false, false));
        });

        SyncEvents.listen(MineAreaChangeEvent.class, event -> {
            XTitles.sendTitle(event.getPrisoner().getPlayer(), 4, 5, 5, "&cEntering...", "&4" + TextUtil.beautify(event.getTo().name()));
        });

        SyncEvents.listen(MineEnterEvent.class, event -> {
            XTitles.sendTitle(event.getPrisoner().getPlayer(), 4, 5, 5, "&cEntering...", "&4" + TextUtil.beautify(event.getMine().getName()));
        });
    }

    public void check(Flag flag, Location location, Cancellable event) {
        Optional<SuperiorMine> mineAt = mineHolder.getMineAt(location);
        if (!mineAt.isPresent())
            return;

        SuperiorMine iSuperiorMine = mineAt.get();
        Area area = iSuperiorMine.getArea(location);
        if (!area.getFlagState(flag))
            event.setCancelled(true);
    }
}
