package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.menu.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PrisonerListener {

    public PrisonerListener() {
        SyncEvents.listen(PlayerQuitEvent.class, event -> {
            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().insertOrGetPrisoner(event.getPlayer());
            if (prisoner.getCurrentMine().isPresent()) {
                prisoner.setLogoutInMine(true);
                prisoner.save(true);
            }
        });

        SyncEvents.listen(BlockBreakEvent.class, EventPriority.HIGHEST, event -> {
            if (event.isCancelled()) return;

            Optional<Prisoner> optionalPrisoner = SuperiorPrisonPlugin.getInstance().getDataController().getPrisoner(event.getPlayer().getUniqueId());
            if (optionalPrisoner.isPresent()) {
                SPrisoner prisoner = (SPrisoner) optionalPrisoner.get();
                if (!prisoner.getCurrentMine().isPresent()) return;

                Set<ItemStack> drops = new HashSet<>(event.getBlock().getDrops());

                if (prisoner.isAutoSell()) {
                    SNormalMine mine = (SNormalMine) prisoner.getCurrentMine().get();
                    mine.getShop().getItems().forEach(shopItem -> {
                        for (ItemStack drop : new HashSet<>(drops)) {
                            if (shopItem.getItem().isSimilar(drop)) {
                                double onePrice = shopItem.getPrice();
                                double finalPrice = onePrice * drop.getAmount();
                                // TODO: Add money

                                drops.remove(drop);
                            }
                        }
                    });
                }

                if (prisoner.isAutoPickup()) {
                    drops.forEach(item -> {
                        int added = InventoryUtil.addItem(item.clone(), event.getPlayer());
                        if (added == item.getAmount())
                            event.getBlock().getDrops().remove(item);

                        else
                            item.setAmount(item.getAmount() - added);
                    });
                }
                event.getBlock().getDrops().clear();
                event.getBlock().getDrops().addAll(drops);
            }
        });

        SyncEvents.listen(BlockBreakEvent.class, EventPriority.MONITOR, event -> {
            if (event.isCancelled()) return;

            Optional<Prisoner> prisonerOptional = SuperiorPrisonPlugin.getInstance().getDataController().getPrisoner(event.getPlayer().getUniqueId());
            if (!prisonerOptional.isPresent()) return;

            SPrisoner prisoner = (SPrisoner) prisonerOptional.get();
            prisoner.getMinedBlocks().putIfPresentUpdate(event.getBlock().getType(), 1L, Long::sum);
        });
    }
}
