package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.SimpleBackPackConfig;
import com.bgsoftware.superiorprison.plugin.menu.backpack.AdvancedBackPackView;
import com.bgsoftware.superiorprison.plugin.menu.backpack.SimpleBackPackView;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.util.PermUtil;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.task.OTask;
import org.bukkit.Material;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackPackListener {
    private final Pattern BACKPACK_UPGRADE_PATTERN = Pattern.compile("prison.backpack.autoupgrade.([^ ]+).([0-9]+)");

    public BackPackListener() {
        SyncEvents.listen(PlayerInteractEvent.class, event -> {
            if (event.getItem() == null || event.getItem().getType() == Material.AIR) return;

            ItemStack itemStack = event.getItem();
            if (!SuperiorPrisonPlugin.getInstance().getBackPackController().isBackPack(itemStack)) return;

            event.setCancelled(true);
            SBackPack backPack = (SBackPack) SuperiorPrisonPlugin.getInstance().getBackPackController().getBackPack(itemStack, event.getPlayer());

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

            event.getPlayer().getInventory().setItem(event.getPlayer().getInventory().first(event.getItem()), null);
            if (backPack.getConfig() instanceof SimpleBackPackConfig) {
                new SimpleBackPackView(SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer()), backPack).open();
                return;
            }

            new AdvancedBackPackView(SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer()), backPack).open();
        });
    }
}
