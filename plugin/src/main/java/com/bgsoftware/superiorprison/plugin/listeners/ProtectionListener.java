package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.flags.FlagEnum;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Optional;

public class ProtectionListener {

    public ProtectionListener(SuperiorPrisonPlugin plugin){
        SyncEvents.listen(EntityDamageByEntityEvent.class, event -> {
            Optional<SuperiorMine> mineAtLocation = plugin.getMineController().getMineAt(event.getDamager().getLocation());

            if (!mineAtLocation.isPresent())
                return;

            SuperiorMine iSuperiorMine = mineAtLocation.get();
            if (iSuperiorMine.isFlag(FlagEnum.PVP))
                event.setCancelled(true);
        });
    }

}
