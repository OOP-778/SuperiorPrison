package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.data.player.booster.DropsBooster;
import com.bgsoftware.superiorprison.api.event.mine.MineBlockBreakEvent;
import com.bgsoftware.superiorprison.api.event.mine.MineEnterEvent;
import com.bgsoftware.superiorprison.api.event.mine.MineLeaveEvent;
import com.bgsoftware.superiorprison.api.event.mine.area.MineAreaChangeEvent;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.mine.effects.SMineEffect;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.SPLocation;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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
                                                        .map(SuperiorMine::getSpawnPoint)
                                                        .ifPresent(location -> event.getPlayer().teleport(location));
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

        SyncEvents.listen(BlockBreakEvent.class, EventPriority.LOWEST, event -> {
            // World check
            Set<String> worldNames = SuperiorPrisonPlugin.getInstance().getMineController().getMinesWorlds();
            if (!worldNames.contains(event.getPlayer().getWorld().getName()))
                return;

            Optional<SuperiorMine> mineAt = SuperiorPrisonPlugin.getInstance().getMineController().getMineAt(event.getBlock().getLocation());
            if (!mineAt.isPresent())
                return;

            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer());
            if (!mineAt.get().canEnter(prisoner)) {
                event.setCancelled(true);
                return;
            }

            if (!prisoner.getCurrentMine().isPresent()) return;
            Pair<SuperiorMine, AreaEnum> minePair = prisoner.getCurrentMine().get();

            SArea area = (SArea) minePair.getKey().getArea(minePair.getValue());
            if (area.getType() != AreaEnum.MINE && !event.getPlayer().hasPermission("superiorprison.flags.bypass")) {
                event.setCancelled(true);
                return;
            }

            if (!area.isInsideWithY(new SPLocation(event.getBlock().getLocation()))) {
                event.setCancelled(true);
                return;
            }

            MineBlockBreakEvent mineBlockBreakEvent = new MineBlockBreakEvent(minePair.getKey(), prisoner, event.getBlock());
            Bukkit.getPluginManager().callEvent(mineBlockBreakEvent);

            if (mineBlockBreakEvent.isCancelled())
                event.setCancelled(true);
        });

        SyncEvents.listen(MineBlockBreakEvent.class, EventPriority.HIGHEST, event -> {
            if (event.isCancelled()) return;

            Player player = event.getPrisoner().getPlayer();
            if (player.getItemInHand().getType() == Material.AIR) return;

            Optional<Prisoner> optionalPrisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(player.getUniqueId());
            if (optionalPrisoner.isPresent()) {
                SPrisoner prisoner = (SPrisoner) optionalPrisoner.get();
                if (!prisoner.getCurrentMine().isPresent()) return;

                Set<ItemStack> drops = new HashSet<>();
                OItem tool = new OItem(player.getItemInHand());

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
                    mine.getShop().getItems().forEach(shopItem -> {
                        for (ItemStack drop : new HashSet<>(drops)) {
                            if (shopItem.getItem().isSimilar(drop)) {
                                BigDecimal price = prisoner.getPrice(drop).multiply(BigDecimal.valueOf(drop.getAmount()));
                                if (price.doubleValue() == 0) continue;

                                SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> VaultHook.class, vault -> vault.depositPlayer(prisoner, price));
                                drops.remove(drop);
                            }
                        }
                    });
                }

                Set<DropsBooster> boosters = prisoner.getBoosters().findBoostersBy(DropsBooster.class);
                if (!boosters.isEmpty()) {
                    double[] rate = new double[]{0};
                    boosters.forEach(booster -> rate[0] = rate[0] + booster.getRate());
                    drops.forEach(itemStack -> itemStack.setAmount((int) Math.round(rate[0] * itemStack.getAmount())));
                }

                // Handle auto pickup
                if (prisoner.isAutoPickup()) {
                    HashMap<Integer, ItemStack> left = player.getInventory().addItem(drops.toArray(new ItemStack[0]));
                    if (left.isEmpty())
                        drops.clear();

                    else {
                        drops.clear();
                        drops.addAll(left.values());

                        LocaleEnum.AUTO_PICKUP_PRISONER_INVENTORY_FULL.getWithErrorPrefix().send(player);
                    }
                }

                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);

                drops.forEach(item -> event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0, 0.5), item));
            }
        });

        SyncEvents.listen(MineEnterEvent.class, EventPriority.LOWEST, event -> {
            if (!event.getMine().canEnter(event.getPrisoner())) {
                event.setCancelled(true);
                return;
            }

            event.getMine().getEffects().get().stream().map(effect -> (SMineEffect) effect).map(SMineEffect::create).forEach(effect -> event.getPrisoner().getPlayer().addPotionEffect(effect, false));
        });

        SyncEvents.listen(MineAreaChangeEvent.class, EventPriority.LOWEST, event -> {
            if (event.getMine().getGenerator().isCaching() || event.getMine().getGenerator().isResetting()) {
                event.setCancelled(true);
                LocaleEnum.CANNOT_ENTER_MINE_MINE_NOT_READY.getWithPrefix().send(event.getPrisoner().getPlayer());
            }
        });

        SyncEvents.listen(MineLeaveEvent.class, EventPriority.LOWEST, event -> event.getMine().getEffects().get().forEach(effect -> event.getPrisoner().getPlayer().removePotionEffect(effect.getType())));
    }

    private int getItemCountWithFortune(Material material, int enchant_level) {
        int drops = ThreadLocalRandom.current().nextInt(enchant_level + 2) - 1;
        if (drops < 0)
            drops = 0;

        int i = material == Material.LAPIS_BLOCK ? 4 + ThreadLocalRandom.current().nextInt(5) : 1;
        return i * (drops + 1);
    }
}
