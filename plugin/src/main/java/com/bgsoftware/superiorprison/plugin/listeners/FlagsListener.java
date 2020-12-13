package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.flags.Flag;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.holders.SMineHolder;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.Set;

public class FlagsListener {
    private final SMineHolder mineHolder;

    public FlagsListener() {
        SuperiorPrisonPlugin plugin = SuperiorPrisonPlugin.getInstance();
        mineHolder = plugin.getMineController();

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

            if (hasBypass(event.getPlayer()))
                event.setCancelled(true);
        });

        // Block enderpearls & interactable blocks
        SyncEvents.listen(PlayerInteractEvent.class, event -> {
            ItemStack item = event.getItem();

            if (item != null && OMaterial.matchMaterial(item) == OMaterial.ENDER_PEARL) {
                Optional<SuperiorMine> mineAt = mineHolder.getMineAt(event.getPlayer().getLocation());
                if (!mineAt.isPresent()) return;

                if (mineAt.get().getSettings().isDisableEnderPearls())
                    event.setCancelled(true);

            } else if (event.getClickedBlock() != null) {
                OMaterial oMaterial = OMaterial.matchMaterial(event.getClickedBlock().getType());
                if (SuperiorPrisonPlugin.getInstance().getMainConfig().getDisabledInteractableBlocks().contains(oMaterial)) {
                    Optional<SuperiorMine> mineAt = mineHolder.getMineAt(event.getPlayer().getLocation());
                    if (!mineAt.isPresent()) return;

                    if (hasBypass(event.getPlayer()))
                        event.setCancelled(true);
                }
            }
        });

        // Block interactable tile entities
        SyncEvents.listen(PlayerInteractEntityEvent.class, event -> {
            if (event.getRightClicked().getType().isAlive()) return;

            // World check
            Set<String> worldNames = mineHolder.getMinesWorlds();
            if (!worldNames.contains(event.getRightClicked().getWorld().getName()))
                return;

            Optional<SuperiorMine> mineAt = mineHolder.getMineAt(event.getPlayer().getLocation());
            if (!mineAt.isPresent()) return;

            OMaterial material = OMaterial.matchMaterial(event.getRightClicked().getType().name());
            if (material == null) {
                String name = event.getRightClicked().getType().name();
                if (name.contains("_")) {
                    String[] split = name.split("_");
                    name = split[1] + "_" + split[0];
                    material = OMaterial.matchMaterial(name);
                    if (material == null) return;
                }
            }

            if (SuperiorPrisonPlugin.getInstance().getMainConfig().getDisabledInteractableBlocks().contains(material)) {
                if (hasBypass(event.getPlayer()))
                    event.setCancelled(true);
            }
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

        if (!area.getFlagState(flag) && hasBypass(player))
            event.setCancelled(true);
    }

    public boolean hasBypass(Player player) {
        return !player.hasPermission("superiorprison.flags.bypass");
    }
}
