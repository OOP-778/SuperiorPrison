package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.menu.backpack.BackPackViewMenu;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BackPackListener {
    public BackPackListener() {
        SyncEvents.listen(PlayerInteractEvent.class, event -> {
            if (event.getItem() == null || event.getItem().getType() == Material.AIR) return;

            ItemStack itemStack = event.getItem();
            if (!SuperiorPrisonPlugin.getInstance().getBackPackController().isBackPack(itemStack)) return;

            event.setCancelled(true);
            SBackPack backPack = (SBackPack) SuperiorPrisonPlugin.getInstance().getBackPackController().getBackPack(itemStack, event.getPlayer());
            new BackPackViewMenu(SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer()), backPack).open();

            event.getPlayer().getInventory().remove(itemStack);
        });
    }
}
