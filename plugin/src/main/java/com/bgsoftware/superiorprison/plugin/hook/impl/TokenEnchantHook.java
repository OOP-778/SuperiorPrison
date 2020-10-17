package com.bgsoftware.superiorprison.plugin.hook.impl;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.event.mine.MultiBlockBreakEvent;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.SHook;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.ClassDebugger;
import com.oop.orangeengine.main.events.SyncEvents;
import com.vk2gpz.tokenenchant.event.TEBlockExplodeEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Optional;

public class TokenEnchantHook extends SHook {
    public TokenEnchantHook() {
        super(null);

        SyncEvents.listen(TEBlockExplodeEvent.class, event -> {
            Optional<SuperiorMine> mineAt = SuperiorPrisonPlugin.getInstance().getMineController().getMineAt(event.getPlayer().getLocation());
            if (!mineAt.isPresent()) return;

            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer().getUniqueId());

            MultiBlockBreakEvent multiBlockBreakEvent = SuperiorPrisonPlugin.getInstance().getBlockController().breakBlock(
                    prisoner,
                    mineAt.get(),
                    event.getItemStack(),
                    event.blockList().stream()
                            .map(Block::getLocation)
                            .filter(location -> mineAt.get().isInside(location))
                            .toArray(Location[]::new)
            );

            multiBlockBreakEvent.getBlockData().values().forEach(item -> ClassDebugger.debug("Left drops: {}", Arrays.toString(item.getValue().toArray(new ItemStack[0]))));
            event.setCancelled(true);

            // At the end clean this shiet
            event.blockList().clear();
        });
    }

    @Override
    public String getPluginName() {
        return "TokenEnchant";
    }
}
