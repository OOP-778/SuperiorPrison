package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.SimpleBackPackConfig;
import com.bgsoftware.superiorprison.plugin.menu.backpack.AdvancedBackPackView;
import com.bgsoftware.superiorprison.plugin.menu.backpack.BackpackLockable;
import com.bgsoftware.superiorprison.plugin.menu.backpack.SimpleBackPackView;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.inventory.PatchedInventory;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.inventory.SPlayerInventory;
import com.bgsoftware.superiorprison.plugin.util.PermUtil;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.task.OTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import javax.sound.midi.Patch;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackPackListener {
    private final Pattern BACKPACK_UPGRADE_PATTERN = Pattern.compile("prison.backpack.autoupgrade.([^ ]+).([0-9]+)");

    public BackPackListener() {
        // Listen for backpack click event (Backpack slot lock)
        SyncEvents.listen(InventoryClickEvent.class, EventPriority.LOWEST, event -> {
            if (event.getClickedInventory() == null) return;
            if (event.getWhoClicked().getOpenInventory().getTopInventory() == null) return;
            if (!(event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof BackpackLockable))
                return;

            SPrisoner viewer =
                    ((BackpackLockable) event.getWhoClicked().getOpenInventory().getTopInventory().getHolder()).getViewer();

            viewer.getOpenedBackpack().ifPresent(pair -> {
                if (event.getSlot() == pair.getFirst())
                    event.setCancelled(true);
            });
        });

        // Listen for backpack click event (Backpack slot lock)
        SyncEvents.listen(InventoryClickEvent.class, EventPriority.LOWEST, event -> {
            if (event.getClickedInventory() == null) return;
            if (event.getWhoClicked().getOpenInventory().getTopInventory() == null) return;
            if (!(event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof AdvancedBackPackView))
                return;

            new OTask()
                    .delay(100)
                    .runnable(() -> ((AdvancedBackPackView) event.getWhoClicked().getOpenInventory().getTopInventory().getHolder()).onUpdate())
                    .execute();
        });

        // Listen for menu
        SyncEvents.listen(PlayerInteractEvent.class, event -> {
            if (event.getItem() == null || event.getItem().getType() == Material.AIR) return;

            ItemStack itemStack = event.getItem();
            if (!SuperiorPrisonPlugin.getInstance().getBackPackController().isBackPack(itemStack)) return;

            event.setCancelled(true);
            SBackPack backPack = (SBackPack) SuperiorPrisonPlugin.getInstance().getBackPackController().getBackPack(itemStack, event.getPlayer());
            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer());

            List<String> permissions = PermUtil.getPermissions(BACKPACK_UPGRADE_PATTERN, event.getPlayer());
            for (String permission : permissions) {
                Matcher matcher = BACKPACK_UPGRADE_PATTERN.matcher(permission);
                while (matcher.find()) {
                    String backpackName = matcher.group(1);
                    String upgradeLevelString = matcher.group(2);

                    if (!backPack.getData().getConfigId().equalsIgnoreCase(backpackName)) break;

                    int upgradeLevel = Integer.parseInt(upgradeLevelString);
                    if (backPack.getCurrentLevel() >= upgradeLevel) break;

                    backPack.upgrade(upgradeLevel);
                }
            }

            prisoner.lockBackpack(event.getPlayer().getInventory().first(event.getItem()), backPack);
            if (backPack.getConfig() instanceof SimpleBackPackConfig) {
                new SimpleBackPackView(prisoner, backPack).open();
                return;
            }

            new AdvancedBackPackView(prisoner, backPack).open();
        });

        // Patch Player inventory
        SyncEvents.listen(PlayerJoinEvent.class, event -> {
            new OTask()
                    .delay(100)
                    .runnable(() -> {
                        if (!event.getPlayer().isOnline()) return;
                        SPlayerInventory.patch(event.getPlayer());
                    })
                    .execute();
        });

        // Listen for player slot switch event
        SyncEvents.listen(InventoryClickEvent.class, EventPriority.HIGHEST, event -> {
            if (event.isCancelled()) return;

            // If the inventory is not a patched one, return
            if (!(event.getClickedInventory() instanceof PatchedInventory)) return;

            SPlayerInventory patchedInventory = ((PatchedInventory) event.getClickedInventory()).getOwner();

            // If pickup update the item
            if (event.getAction().name().contains("PICKUP")) {
                SBackPack sBackPack = patchedInventory.getBackPackMap().get(event.getSlot());
                if (sBackPack != null) {
                    event.setCancelled(true);

                    sBackPack.save();
                    ItemStack itemStack = sBackPack.updateManually();

                    patchedInventory.getBackPackMap().remove(event.getSlot());
                    event.getClickedInventory().setItem(event.getSlot(), null);
                    event.getWhoClicked().setItemOnCursor(itemStack);
                }
            }

            new OTask()
                    .delay(100)
                    .runnable(patchedInventory::init)
                    .execute();
        });

        // Listen for drop event
        SyncEvents.listen(PlayerDropItemEvent.class, EventPriority.HIGHEST, event -> {
            if (event.isCancelled()) return;
            if (!(event.getPlayer().getInventory() instanceof PatchedInventory)) return;

            SPlayerInventory inventory = ((PatchedInventory)event.getPlayer().getInventory()).getOwner();

            SBackPack backPackBy = inventory.findBackPackBy(event.getItemDrop().getItemStack());
            if (backPackBy == null) return;

            inventory.removeItem(event.getItemDrop().getItemStack());

            if (backPackBy.isModified()) {
                backPackBy.save();
                event.getItemDrop().setItemStack(backPackBy.updateManually());
            }

            SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Player {} Dropped Backpack", event.getPlayer().getName());
        });

        SyncEvents.listen(PlayerPickupItemEvent.class, EventPriority.HIGHEST, event -> {
            if (event.isCancelled()) return;
            if (!(event.getPlayer().getInventory() instanceof PatchedInventory)) return;

            new OTask()
                    .delay(100)
                    .runnable(() -> {
                        SPlayerInventory inventory = ((PatchedInventory)event.getPlayer().getInventory()).getOwner();
                        inventory.init();
                    })
                    .execute();
        });

        SyncEvents.listen(PlayerQuitEvent.class, event -> {
            if (!(event.getPlayer().getInventory() instanceof PatchedInventory)) return;

            ((PatchedInventory) event.getPlayer().getInventory()).getOwner().getBackPackMap().forEach((key, backpack) -> {
                backpack.save();
                event.getPlayer().getInventory().setItem(key, backpack.updateManually());
            });

            ((PatchedInventory) event.getPlayer().getInventory()).getOwner().getBackPackMap().clear();
        });
    }
}
