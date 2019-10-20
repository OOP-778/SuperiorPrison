package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.events.MineEnterEvent;
import com.bgsoftware.superiorprison.api.events.MineLeaveEvent;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;
import java.util.Set;

public class MineListener {

    public MineListener() {
        SuperiorPrisonPlugin plugin = SuperiorPrisonPlugin.getInstance();

        // Mine Leave & Enter events handling
        SyncEvents.listen(PlayerMoveEvent.class, event -> {
            if (event.getFrom() == event.getTo()) return;

            Set<String> worldNames = SuperiorPrisonPlugin.getInstance().getMineController().getMinesWorlds();
            if (!worldNames.contains(event.getPlayer().getWorld().getName()))
                return;

            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().insertOrGetPrisoner(event.getPlayer());
            Optional<SuperiorMine> mineOptional = prisoner.getCurrentMine();

            if (mineOptional.isPresent()) {
                SuperiorMine mine = mineOptional.get();
                if (!mine.isInside(event.getTo())) {

                    MineLeaveEvent leaveEvent = new MineLeaveEvent(mine, prisoner);
                    Bukkit.getPluginManager().callEvent(leaveEvent);

                    mine.getPrisoners().remove(prisoner);
                    prisoner.setCurrentMine(null);
                }
            } else {

                Optional<SuperiorMine> mineAt = SuperiorPrisonPlugin.getInstance().getMineController().getMineAt(event.getTo());
                if (mineAt.isPresent()) {
                    SuperiorMine mine = mineAt.get();

                    MineEnterEvent enterEvent = new MineEnterEvent(mine, prisoner);
                    Bukkit.getPluginManager().callEvent(enterEvent);

                    mine.getPrisoners().add(prisoner);
                    prisoner.setCurrentMine(mine);
                }
            }
        });

    }

}
