package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.events.MineEnterEvent;
import com.bgsoftware.superiorprison.api.events.MineLeaveEvent;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;
import java.util.Set;

public class MineListener {

    public MineListener() {

        // Mine Leave & Enter events handling
        SyncEvents.listen(PlayerMoveEvent.class, event -> {
            // Checks if the player actually moved a block.
            Location from = event.getFrom(), to = event.getTo();
            if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
                return;

            // World check
            Set<String> worldNames = SuperiorPrisonPlugin.getInstance().getMineController().getMinesWorlds();
            if (!worldNames.contains(event.getPlayer().getWorld().getName()))
                return;

            // Get prisoner
            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().insertOrGetPrisoner(event.getPlayer());
            Optional<SuperiorMine> mineOptional = prisoner.getCurrentMine();

            if (mineOptional.isPresent()) {
                SuperiorMine mine = mineOptional.get();
                if (!mine.isInside(event.getTo())) {
                    Bukkit.broadcastMessage("Leaving mine");
                    MineLeaveEvent leaveEvent = new MineLeaveEvent(mine, prisoner);
                    Bukkit.getPluginManager().callEvent(leaveEvent);

                    mine.getPrisoners().remove(prisoner);
                    prisoner.setCurrentMine(null);
                }

            } else {
                Optional<SuperiorMine> mineAt = SuperiorPrisonPlugin.getInstance().getMineController().getMineAt(event.getTo());
                if (mineAt.isPresent()) {
                    SuperiorMine mine = mineAt.get();

                    Bukkit.broadcastMessage("Entering mine");
                    MineEnterEvent enterEvent = new MineEnterEvent(mine, prisoner);
                    Bukkit.getPluginManager().callEvent(enterEvent);

                    mine.getPrisoners().add(prisoner);
                    prisoner.setCurrentMine(mine);
                }
            }
        });

        SyncEvents.listen(PlayerTeleportEvent.class, event -> {

            // World check
            Set<String> worldNames = SuperiorPrisonPlugin.getInstance().getMineController().getMinesWorlds();
            if (!worldNames.contains(event.getFrom().getWorld().getName()) && !worldNames.contains(event.getTo().getWorld().getName()))
                return;

            // Get prisoner
            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().insertOrGetPrisoner(event.getPlayer());

            // Get mine
            Optional<SuperiorMine> mineOptional = prisoner.getCurrentMine();

            // Check if prisoner is leaving mine
            if (mineOptional.isPresent()) {

                if (mineOptional.get().isInside(event.getTo()))
                    return;

                SuperiorMine mine = mineOptional.get();
                MineLeaveEvent leaveEvent = new MineLeaveEvent(mine, prisoner);
                Bukkit.getPluginManager().callEvent(leaveEvent);

                if (leaveEvent.isCancelled())
                    event.setCancelled(true);

                else {
                    mine.getPrisoners().remove(prisoner);
                    prisoner.setCurrentMine(null);
                }
            }

            // Checking if mine exists at event#getTo()
            mineOptional = SuperiorPrisonPlugin.getInstance().getMineController().getMineAt(event.getTo());
            if (mineOptional.isPresent()) {
                SuperiorMine mine = mineOptional.get();

                MineEnterEvent enterEvent = new MineEnterEvent(mine, prisoner);
                Bukkit.getPluginManager().callEvent(enterEvent);

                mine.getPrisoners().add(prisoner);
                prisoner.setCurrentMine(mine);
            }
        });

    }
}
