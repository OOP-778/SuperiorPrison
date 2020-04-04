package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.event.mine.MineEnterEvent;
import com.bgsoftware.superiorprison.api.event.mine.MineLeaveEvent;
import com.bgsoftware.superiorprison.api.event.mine.area.MineAreaChangeEvent;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.SPair;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.Set;

import static com.oop.orangeengine.main.events.AsyncEvents.async;

public class MineListener {

    public MineListener() {
        SMineHolder mineHolder = SuperiorPrisonPlugin.getInstance().getDatabaseController().getMineHolder();
        SPrisonerHolder prisonerHolder = SuperiorPrisonPlugin.getInstance().getDatabaseController().getPrisonerHolder();

        // Disallow block place event if no perm
        SyncEvents.listen(BlockBreakEvent.class, EventPriority.LOWEST, event -> {
            if (event.isCancelled()) return;

            // World check if should make it a bit lighter
            if (!mineHolder.getMinesWorlds().contains(event.getPlayer().getLocation().getWorld().getName()))
                return;

            // If prisoner isn't in a mine return
            Prisoner prisoner = prisonerHolder.getInsertIfAbsent(event.getPlayer());
            if (!prisoner.getCurrentMine().isPresent()) return;

            SNormalMine superiorMine = (SNormalMine) prisoner.getCurrentMine().get().getKey();
            AreaEnum areaTypeAt = superiorMine.getAreaTypeAt(event.getBlock().getLocation());
            if (areaTypeAt == AreaEnum.MINE) {
                superiorMine.getGenerator().setNonEmptyBlocks(superiorMine.getGenerator().getNonEmptyBlocks() - 1);
                superiorMine.save(true);

                if (superiorMine.getSettings().getResetSettings().isTimed()) return;
                async(() -> {
                    int percentageOfFullBlocks = superiorMine.getGenerator().getPercentageOfFullBlocks();
                    int percentageRequired = superiorMine.getSettings().getResetSettings().asPercentage().getRequiredPercentage();

                    System.out.println("Percentage Required: " + percentageRequired);
                    System.out.println("Current: " + percentageOfFullBlocks);
                    System.out.println("Non Emtpy Blocks: " + superiorMine.getGenerator().getNonEmptyBlocks());

                    if (percentageOfFullBlocks <= percentageRequired) {
                        superiorMine.getGenerator().setNonEmptyBlocks(superiorMine.getGenerator().getCachedMaterials().length);
                        superiorMine.getGenerator().reset();
                    }
                });

            }
        });

        // Mine Leave & Enter events handling
        SyncEvents.listen(PlayerMoveEvent.class, event -> {
            // Checks if the player actually moved a block.
            Location from = event.getFrom(), to = event.getTo();
            if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
                return;

            // World check
            Set<String> worldNames = mineHolder.getMinesWorlds();
            if (!worldNames.contains(event.getPlayer().getWorld().getName()))
                return;

            // Get prisoner
            SPrisoner prisoner = prisonerHolder.getInsertIfAbsent(event.getPlayer());
            Optional<Pair<SuperiorMine, AreaEnum>> mineOptional = prisoner.getCurrentMine();

            if (mineOptional.isPresent()) {
                SPair<SuperiorMine, AreaEnum> mine = (SPair<SuperiorMine, AreaEnum>) mineOptional.get();
                AreaEnum currentAt = mine.getKey().getAreaTypeAt(event.getTo());
                if (currentAt == null) {

                    MineLeaveEvent leaveEvent = new MineLeaveEvent(mine.getKey(), prisoner, null);
                    Bukkit.getPluginManager().callEvent(leaveEvent);

                    mine.getKey().getPrisoners().remove(prisoner);
                    prisoner.setCurrentMine(null);

                } else if (currentAt != mine.getValue()) {
                    MineAreaChangeEvent areaChangeEvent = new MineAreaChangeEvent(prisoner, mine.getKey().getAreaTypeAt(event.getFrom()), currentAt, mine.getKey());
                    Bukkit.getPluginManager().callEvent(areaChangeEvent);

                    if (areaChangeEvent.isCancelled()) {
                        Vector vector = event.getPlayer().getLocation().toVector().subtract(event.getTo().toVector()).normalize();
                        event.getPlayer().setVelocity(vector.multiply(0.5));
                        return;
                    }

                    mine.setValue(currentAt);
                }

            } else {
                Optional<SuperiorMine> mineAt = SuperiorPrisonPlugin.getInstance().getMineController().getMineAt(event.getTo());
                if (mineAt.isPresent()) {
                    SuperiorMine mine = mineAt.get();

                    MineEnterEvent enterEvent = new MineEnterEvent(mine, prisoner, mine.getArea(event.getTo()));
                    Bukkit.getPluginManager().callEvent(enterEvent);

                    if (enterEvent.isCancelled()) {
                        Vector vector = event.getPlayer().getLocation().toVector().subtract(event.getTo().toVector().add(new Vector(0, 1, 0))).normalize();
                        event.getPlayer().setVelocity(vector.multiply(0.8));
                        return;
                    }

                    mine.getPrisoners().add(prisoner);
                    prisoner.setCurrentMine(new SPair<>(mine, mine.getAreaTypeAt(event.getTo())));
                }
            }
        });

        SyncEvents.listen(PlayerTeleportEvent.class, event -> {
            // World check
            Set<String> worldNames = mineHolder.getMinesWorlds();
            if (!worldNames.contains(event.getFrom().getWorld().getName()) && !worldNames.contains(event.getTo().getWorld().getName()))
                return;

            // Get prisoner
            SPrisoner prisoner = prisonerHolder.getInsertIfAbsent(event.getPlayer());

            // Check if prisoner is leaving mine
            prisoner.getCurrentMine().ifPresent(mine -> {
                if (mine.getKey().isInside(event.getTo()))
                    return;

                MineLeaveEvent leaveEvent = new MineLeaveEvent(mine.getKey(), prisoner, null);
                Bukkit.getPluginManager().callEvent(leaveEvent);

                if (leaveEvent.isCancelled()) {
                    event.setCancelled(true);

                } else {
                    mine.getKey().getPrisoners().remove(prisoner);
                    prisoner.setCurrentMine(null);
                }
            });

            // Checking if mine exists at event#getTo()
            SuperiorPrisonPlugin.getInstance().getMineController().getMineAt(event.getTo()).ifPresent(mine -> {
                if (!mine.canEnter(prisoner)) {
                    // TODO: Message
                    return;
                }

                MineEnterEvent enterEvent = new MineEnterEvent(mine, prisoner, mine.getArea(event.getTo()));
                Bukkit.getPluginManager().callEvent(enterEvent);

                if (enterEvent.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }

                mine.getPrisoners().add(prisoner);
                prisoner.setCurrentMine(new SPair<>(mine, mine.getAreaTypeAt(event.getTo())));
            });
        });

        SyncEvents.listen(MineEnterEvent.class, EventPriority.LOWEST, event -> event.setCancelled(!event.getMine().canEnter(event.getPrisoner())));
    }
}
