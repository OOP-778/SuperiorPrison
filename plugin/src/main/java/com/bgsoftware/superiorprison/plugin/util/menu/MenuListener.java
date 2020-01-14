package com.bgsoftware.superiorprison.plugin.util.menu;

import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MenuListener {

    public MenuListener() {
        SyncEvents.listen(InventoryClickEvent.class, event -> {
            if (event.isCancelled()) return;
            if (event.getSlot() == -1) return;
            if (event.getWhoClicked().getOpenInventory().getTopInventory() == null) return;
            if (!(event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof OMenu)) return;

            OMenu menu = (OMenu) event.getWhoClicked().getOpenInventory().getTopInventory().getHolder();

            // Look for drag
            if (event.getInventory().getHolder() != menu)
                menu.handleDrag(event);
            if (event.isCancelled()) return;

            event.setCancelled(true);
            menu.handleClick(event);
        });

        SyncEvents.listen(InventoryCloseEvent.class, event -> {
           if (!(event.getInventory().getHolder() instanceof OMenu)) return;
            ((OMenu)event.getInventory().getHolder()).closeInventory();
        });
    }

}
