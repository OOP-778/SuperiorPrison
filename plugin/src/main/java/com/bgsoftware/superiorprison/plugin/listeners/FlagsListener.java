package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.mine.flags.Flag;
import com.bgsoftware.superiorprison.api.event.mine.MineEnterEvent;
import com.bgsoftware.superiorprison.api.event.mine.area.MineAreaChangeEvent;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.bgsoftware.superiorprison.plugin.util.XTitles;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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
    private SPrisonerHolder prisonerHolder;

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

        // Break flag
        SyncEvents.listen(BlockBreakEvent.class, event -> {
            Optional<SuperiorMine> mineAt = mineHolder.getMineAt(event.getBlock().getLocation());
            if (!mineAt.isPresent())
                return;

            SPrisoner prisoner = prisonerHolder.getInsertIfAbsent(event.getPlayer());
            if (!mineAt.get().canEnter(prisoner))
                event.setCancelled(true);

            SArea area = (SArea) mineAt.get().getArea(event.getBlock().getLocation());
            if (area.getType() == AreaEnum.MINE && area.getMinPoint().getBlockY() > event.getBlock().getLocation().getY())
                event.setCancelled(true);

            if (area.getType() != AreaEnum.MINE && !hasBypass(event.getPlayer()))
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

        // Night Vision flag
        SyncEvents.listen(MineEnterEvent.class, event -> {
            Area area = event.getArea();
            if (area.getFlagState(Flag.NIGHT_VISION))
                event.getPrisoner().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999999, 1, false, false));
        });

        SyncEvents.listen(MineAreaChangeEvent.class, event -> XTitles.sendTitle(event.getPrisoner().getPlayer(), 4, 5, 5, "&cEntering...", "&4" + TextUtil.beautify(event.getTo().name())));
        SyncEvents.listen(MineEnterEvent.class, event -> XTitles.sendTitle(event.getPrisoner().getPlayer(), 4, 5, 5, "&cEntering...", "&4" + TextUtil.beautify(event.getMine().getName())));
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
