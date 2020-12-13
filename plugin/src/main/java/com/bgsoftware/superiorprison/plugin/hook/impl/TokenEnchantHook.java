package com.bgsoftware.superiorprison.plugin.hook.impl;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.SHook;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.SPLocation;
import com.oop.orangeengine.main.events.SyncEvents;
import com.vk2gpz.tokenenchant.event.TEBlockExplodeEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Optional;

public class TokenEnchantHook extends SHook {
    public TokenEnchantHook() {
        super(null);

        SyncEvents.listen(TEBlockExplodeEvent.class, event -> {
            Optional<SuperiorMine> mineAt = SuperiorPrisonPlugin.getInstance().getMineController().getMineAt(event.getPlayer().getLocation());
            if (!mineAt.isPresent()) return;

            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer().getUniqueId());
            SArea area = (SArea) mineAt.get().getArea(AreaEnum.MINE);

            if (!SuperiorPrisonPlugin.getInstance().getMainConfig().isGiveFullControlToTE()) {
                SuperiorPrisonPlugin.getInstance().getBlockController().breakBlock(
                        prisoner,
                        mineAt.get(),
                        event.getItemStack(),
                        event.blockList().stream()
                                .map(Block::getLocation)
                                .filter(location -> area.isInsideWithY(new SPLocation(location), true))
                                .toArray(Location[]::new)
                );

                event.setCancelled(true);

                // At the end clean this shiet
                event.blockList().clear();
                return;
            }

            event.blockList().removeIf(block -> {
                boolean remove = !area.isInsideWithoutY(block.getLocation());
                if (remove)
                    mineAt.get().getGenerator().getBlockData().remove(block.getLocation());

                return remove;
            });
        });
    }

    @Override
    public String getPluginName() {
        return "TokenEnchant";
    }
}
