package com.bgsoftware.superiorprison.plugin.util.menu;

import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class MenuListener {

    public MenuListener() {
        SyncEvents.listen(InventoryClickEvent.class, event -> {
            if (event.isCancelled()) return;
            if (event.getSlot() == -1) return;
            if (event.getClickedInventory() == null) return;
            if (event.getWhoClicked().getOpenInventory().getTopInventory() == null) return;
            if (!(event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof OMenu)) return;

            OMenu menu = (OMenu) event.getWhoClicked().getOpenInventory().getTopInventory().getHolder();
            event.setCancelled(true);

            if (!(event.getClickedInventory().getHolder() instanceof OMenu)) {
                menu.handleBottomClick(event);
                return;
            }

            if (event.getClickedInventory().getHolder() == menu)
                menu.handleClick(event);
        });

        SyncEvents.listen(InventoryCloseEvent.class, event -> {
            if (!(event.getInventory().getHolder() instanceof OMenu)) return;
            ((OMenu) event.getInventory().getHolder()).closeInventory(event);
        });

        SyncEvents.listen(InventoryDragEvent.class, event -> {
            if (event.getWhoClicked().getOpenInventory().getTopInventory() == null) return;
            if (!(event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof OMenu)) return;

            OMenu menu = (OMenu) event.getWhoClicked().getOpenInventory().getTopInventory().getHolder();
            menu.handleDrag(event);
        });
    }

}
