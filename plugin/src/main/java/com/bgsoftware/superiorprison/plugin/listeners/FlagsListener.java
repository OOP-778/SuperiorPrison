package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.flags.FlagEnum;
import com.bgsoftware.superiorprison.api.events.MineEnterEvent;
import com.bgsoftware.superiorprison.api.events.MineLeaveEvent;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

public class FlagsListener {

    public FlagsListener() {
        SuperiorPrisonPlugin plugin = SuperiorPrisonPlugin.getInstance();

        // PvP flag listener
        SyncEvents.listen(EntityDamageByEntityEvent.class, event -> {
            Optional<SuperiorMine> mineAtLocation = plugin.getMineController().getMineAt(event.getDamager().getLocation());

            if (!mineAtLocation.isPresent())
                return;

            SuperiorMine iSuperiorMine = mineAtLocation.get();
            if (!iSuperiorMine.isFlag(FlagEnum.PVP))
                event.setCancelled(true);
        });

        // Night Vision flag listeners
        SyncEvents.listen(MineEnterEvent.class, event -> {

            if (event.getMine().isFlag(FlagEnum.NIGHT_VISION))
                event.getPrisoner().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 9999999, 1, false, false));

        });

        SyncEvents.listen(MineLeaveEvent.class, event -> {

            if (event.getMine().isFlag(FlagEnum.NIGHT_VISION))
                event.getPrisoner().getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);

        });

    }

}
