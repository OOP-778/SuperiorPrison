package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.util.version.OVersion;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class MutliVerUtil {
  public static boolean isPrimaryHand(PlayerInteractEvent event) {
    return OVersion.isBefore(9) || event.getHand() != EquipmentSlot.OFF_HAND;
  }
}
