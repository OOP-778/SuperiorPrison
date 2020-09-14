package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.SimpleBackPackConfig;
import com.bgsoftware.superiorprison.plugin.menu.backpack.AdvancedBackPackView;
import com.bgsoftware.superiorprison.plugin.menu.backpack.BackpackLockable;
import com.bgsoftware.superiorprison.plugin.menu.backpack.SimpleBackPackView;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.PermUtil;
import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackPackListener {
    private final Pattern BACKPACK_UPGRADE_PATTERN = Pattern.compile("prison.backpack.autoupgrade.([^ ]+).([0-9]+)");

    public BackPackListener() {
        SyncEvents.listen(InventoryClickEvent.class, EventPriority.LOWEST, event -> {
            if (event.getClickedInventory() == null) return;
            if (event.getWhoClicked().getOpenInventory().getTopInventory() == null) return;
            if (!(event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof BackpackLockable)) return;

            SPrisoner viewer =
                    ((BackpackLockable) event.getWhoClicked().getOpenInventory().getTopInventory().getHolder()).getViewer();

            viewer.getOpenedBackpack().ifPresent(pair -> {
                if (event.getSlot() == pair.getFirst()) {
                    event.setCancelled(true);
                }
            });
        });

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
    }
}
