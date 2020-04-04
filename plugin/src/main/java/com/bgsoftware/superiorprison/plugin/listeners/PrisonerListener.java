package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static com.oop.orangeengine.main.Engine.getEngine;

public class PrisonerListener {
    public PrisonerListener() {
        SyncEvents.listen(PlayerJoinEvent.class, event -> {
            if (!SuperiorPrisonPlugin.getInstance().getRankController().isLoaded()) {
                event.getPlayer().kickPlayer(Helper.color("&cPrison Ranks aren't loaded yet, please wait a bit."));
                return;
            }

            // Check for teleport!
            SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(event.getPlayer().getUniqueId())
                    .map(prisoner -> (SPrisoner) prisoner)
                    .ifPresent(prisoner -> {
                                if (prisoner.isLoggedOutInMine()) {
                                    new OTask()
                                            .delay(1)
                                            .runnable(() -> {
                                                SuperiorPrisonPlugin.getInstance().getMineController()
                                                        .getMine(prisoner.getLogoutMine())
                                                        .flatMap(SuperiorMine::getSpawnPoint)
                                                        .ifPresent(location -> event.getPlayer().teleport(location.toBukkit()));
                                                prisoner.setLogoutMine(null);
                                            })
                                            .execute();
                                }
                            }
                    );
        });

        SyncEvents.listen(PlayerQuitEvent.class, event -> {
            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getDatabaseController().getPrisonerHolder().getInsertIfAbsent(event.getPlayer());
            prisoner.getCurrentMine().ifPresent(mine -> {
                prisoner.setLogoutMine(mine.getKey().getName());
                prisoner.save(true);
            });
        });

        SyncEvents.listen(BlockBreakEvent.class, EventPriority.HIGHEST, event -> {
            if (event.isCancelled()) return;
            if (event.getPlayer().getItemInHand().getType() == Material.AIR) return;

            Optional<Prisoner> optionalPrisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(event.getPlayer().getUniqueId());
            if (optionalPrisoner.isPresent()) {
                SPrisoner prisoner = (SPrisoner) optionalPrisoner.get();
                if (!prisoner.getCurrentMine().isPresent()) return;

                Set<ItemStack> drops = new HashSet<>();
                OItem tool = new OItem(event.getPlayer().getItemInHand());

                if (tool.hasEnchant(Enchantment.SILK_TOUCH))
                    drops.add(new ItemStack(event.getBlock().getType(), 1, event.getBlock().getData()));

                else
                    drops.addAll(event.getBlock().getDrops());

                // Handle auto burn
                if (prisoner.isAutoBurn()) {
                    new HashSet<>(drops)
                            .stream()
                            .filter(item -> item.getType().isBlock())
                            .filter(item -> OMaterial.matchMaterial(item).name().contains("ORE"))
                            .forEach(item -> {
                                OMaterial type = OMaterial.matchMaterial(item);
                                if (type == OMaterial.GOLD_ORE) {
                                    drops.remove(item);
                                    drops.add(OMaterial.GOLD_INGOT.parseItem());
                                }

                                if (type == OMaterial.IRON_ORE) {
                                    drops.remove(item);
                                    drops.add(OMaterial.IRON_INGOT.parseItem());
                                }
                            });
                }

                // Handle Fortune
                if (tool.hasEnchant(Enchantment.LOOT_BONUS_BLOCKS))
                    for (ItemStack itemStack : drops) {
                        if (!prisoner.isFortuneBlocks() && itemStack.getType().isBlock()) continue;

                        itemStack.setAmount(getItemCountWithFortune(itemStack.getType(), tool.getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS)));
                    }

                // Handle auto sell
                if (prisoner.isAutoSell()) {
                    SNormalMine mine = (SNormalMine) prisoner.getCurrentMine().get().getKey();
                    getEngine().getLogger().printDebug("Prisoner Auto Sell");

                    mine.getShop().getItems().forEach(shopItem -> {
                        for (ItemStack drop : new HashSet<>(drops)) {
                            if (shopItem.getItem().isSimilar(drop)) {
                                double onePrice = shopItem.getPrice();
                                double finalPrice = onePrice * drop.getAmount();

                                getEngine().getLogger().printDebug("Found sell time for " + finalPrice);
                                SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> VaultHook.class, vault -> vault.getEcoProvider().depositPlayer(event.getPlayer(), finalPrice));
                                drops.remove(drop);
                            }
                        }
                    });
                }

                // Handle auto pickup
                if (prisoner.isAutoPickup()) {
                    HashMap<Integer, ItemStack> left = event.getPlayer().getInventory().addItem(drops.toArray(new ItemStack[0]));
                    if (left.isEmpty())
                        drops.clear();
                    else
                        left.forEach((amount, item) -> System.out.println("Left " + item + " " + amount));

                }

                System.out.println("Prisoner Check");
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);

                drops.forEach(item -> event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0, 0.5), item));
            }
        });

        SyncEvents.listen(BlockBreakEvent.class, EventPriority.LOW, event -> {
            if (event.isCancelled()) return;

            Optional<Prisoner> prisonerOptional = SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(event.getPlayer().getUniqueId());
            if (!prisonerOptional.isPresent()) return;

            SPrisoner prisoner = (SPrisoner) prisonerOptional.get();
            prisoner.getMinedBlocks().putIfPresentUpdate(event.getBlock().getType(), 1L, Long::sum);
            prisoner.save(true);
        });
    }

    private int getItemCountWithFortune(Material material, int enchant_level) {
        int drops = ThreadLocalRandom.current().nextInt(enchant_level + 2) - 1;
        if (drops < 0)
            drops = 0;

        int i = material == Material.LAPIS_BLOCK ? 4 + ThreadLocalRandom.current().nextInt(5) : 1;
        return i * (drops + 1);
    }
}
