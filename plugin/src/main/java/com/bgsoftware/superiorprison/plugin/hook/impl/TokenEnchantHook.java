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
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class TokenEnchantHook extends SHook {
  public TokenEnchantHook() {
    super(null);

    SyncEvents.listen(
        TEBlockExplodeEvent.class,
        event -> {
          Optional<SuperiorMine> mineAt =
              SuperiorPrisonPlugin.getInstance()
                  .getMineController()
                  .getMineAt(event.getPlayer().getLocation());
          if (!mineAt.isPresent()) return;

          SPrisoner prisoner =
              SuperiorPrisonPlugin.getInstance()
                  .getPrisonerController()
                  .getInsertIfAbsent(event.getPlayer().getUniqueId());
          SArea area = (SArea) mineAt.get().getArea(AreaEnum.MINE);

          SuperiorPrisonPlugin.getInstance()
              .getBlockController()
              .breakBlock(
                  prisoner,
                  mineAt.get(),
                  event.getItemStack(),
                  true,
                  event.blockList().stream()
                      .map(Block::getLocation)
                      .filter(location -> area.isInsideWithY(new SPLocation(location), true))
                      .toArray(Location[]::new));

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
