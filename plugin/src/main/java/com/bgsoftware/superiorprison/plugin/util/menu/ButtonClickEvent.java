package com.bgsoftware.superiorprison.plugin.util.menu;

import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ButtonClickEvent extends InventoryClickEvent {

  @Getter private final OMenuButton button;

  @Getter private final OMenu menu;

  public ButtonClickEvent(InventoryClickEvent event, OMenuButton button) {
    super(
        event.getView(),
        event.getSlotType(),
        event.getRawSlot(),
        event.getClick(),
        event.getAction());
    this.button = button;
    this.menu = (OMenu) event.getWhoClicked().getOpenInventory().getTopInventory().getHolder();
  }
}
