package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
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
import com.bgsoftware.superiorprison.plugin.Updater;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.object.chat.ChatFormat;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.mine.effects.SMineEffect;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.util.SPair;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.cache.OCache;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.message.OMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class PrisonerListener {
    public static OCache<BlockBreakEvent, Boolean> ignoreEvents = OCache
            .builder()
            .concurrencyLevel(1)
            .expireAfter(5, TimeUnit.SECONDS)
            .build();

    public PrisonerListener() {
        SyncEvents.listen(PlayerJoinEvent.class, event -> {
            new OTask()
                    .delay(TimeUnit.SECONDS, 1)
                    .runnable(() -> {
                        if (!event.getPlayer().isOnline()) return;

                        // Check for teleport!
                        SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer());
                        if (prisoner.isLoggedOutInMine()) {
                            new OTask()
                                    .delay(1)
                                    .runnable(() -> {
                                        SuperiorPrisonPlugin.getInstance().getMineController()
                                                .getMine(prisoner.getLogoutMine())
                                                .map(SuperiorMine::getSpawnPoint)
                                                .ifPresent(location -> Framework.FRAMEWORK.teleport(event.getPlayer(), location));
                                        prisoner.setLogoutMine(null);
                                    })
                                    .execute();
                        }

                        // Check for OP
                        if ((event.getPlayer().isOp() || event.getPlayer().hasPermission("prison.admin.updates")) && Updater.isOutdated()) {
                            event.getPlayer().sendMessage(" ");
                            event.getPlayer().sendMessage(Helper.color("&dA new update for SuperiorPrison is available!"));
                            event.getPlayer().sendMessage(Helper.color("&d&l* &7Version: &d" + Updater.getLatestVersion()));
                            event.getPlayer().sendMessage(Helper.color("&d&l* &7Description: &d" + Updater.getVersionDescription()));
                            event.getPlayer().sendMessage(" ");
                        }

                        // Check for big boys
                        if (event.getPlayer().getUniqueId().toString().equals("45713654-41bf-45a1-aa6f-00fe6598703b") || event.getPlayer().getUniqueId().toString().equals("d4f30fc3-b65d-4a66-934a-1e6e4ec439d9")) {
                            StaticTask.getInstance().ensureSync(() -> {
                                event.getPlayer().sendMessage(Helper.color("&8[&fSuperiorSeries&8] &7This server is using SuperiorPrison v" + SuperiorPrisonPlugin.getInstance().getDescription().getVersion()));
                            });
                        }
                    })
                    .execute();
        });

        SyncEvents.listen(EntityDamageEvent.class, event -> {
            if (!(event.getEntity() instanceof Player)) return;
            Player player = (Player) event.getEntity();

            if (player.hasMetadata("NPC")) return;

            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);
            if (!prisoner.getCurrentMine().isPresent()) return;

            if (event.getCause().name().contains("FALL"))
                event.setCancelled(true);
        });

        SyncEvents.listen(PlayerQuitEvent.class, event -> {
            SuperiorPrisonPlugin.getInstance().getDatabaseController().getPrisonerHolder().getPrisoner(event.getPlayer().getUniqueId()).map(prisoner -> (SPrisoner) prisoner).ifPresent(prisoner -> {
                prisoner.getCurrentMine().ifPresent(mine -> {
                    prisoner.setLogoutMine(mine.getKey().getName());
                    prisoner.save(true);

                    prisoner.getCurrentMine().get().getKey().getPrisoners().remove(prisoner);
                    prisoner.setCurrentMine(null);
                });

                prisoner.clearCache();
            });
        });

        SyncEvents.listen(BlockBreakEvent.class, EventPriority.LOWEST, event -> {
            if (ignoreEvents.get(event) != null) return;

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

            // Prisoner is modifying region environment
            if (area.getType() == AreaEnum.REGION) {
                if (!event.getPlayer().hasPermission("prison.admin.bypass"))
                    event.setCancelled(true);
                return;
            }

            // Check if prisoner is breaking under the mine
            if (!area.isInsideWithY(new SPLocation(event.getBlock().getLocation()), true)) {
                event.setCancelled(true);
                return;
            }

            // Check when prisoner mines in a different area
            if (minePair.getKey().getArea(event.getBlock().getLocation()).getType() != area.getType()) {
                event.setCancelled(true);
                return;
            }

            MineBlockBreakEvent mineBlockBreakEvent = new MineBlockBreakEvent(minePair.getKey(), prisoner, event.getBlock());
            Bukkit.getPluginManager().callEvent(mineBlockBreakEvent);

            if (mineBlockBreakEvent.isCancelled()) {
                event.setExpToDrop(0);
                event.setCancelled(true);
            }
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

                BlockBreakEvent bouncedEvent = new BlockBreakEvent(event.getBlock(), event.getPrisoner().getPlayer());
                ignoreEvents.put(bouncedEvent, true);

                Bukkit.getPluginManager().callEvent(bouncedEvent);

                if (bouncedEvent.isCancelled()) {
                    event.getBlock().getDrops().clear();
                    event.setCancelled(true);
                    event.getBlock().setType(Material.AIR);
                    return;
                }

                if (tool.hasEnchant(Enchantment.SILK_TOUCH))
                    drops.add(new ItemStack(event.getBlock().getType(), 1, event.getBlock().getData()));

                else
                    drops.addAll(event.getBlock().getDrops(new ItemStack(tool.getItemStack())));


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

                Set<DropsBooster> boosters = prisoner.getBoosters().findBoostersBy(DropsBooster.class);
                if (!boosters.isEmpty()) {
                    double[] rate = new double[]{0};
                    boosters.forEach(booster -> rate[0] = rate[0] + booster.getRate());
                    drops.forEach(itemStack -> itemStack.setAmount((int) Math.round(rate[0] * itemStack.getAmount())));
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
                                SPair<BigDecimal, Long> soldData = prisoner.getSoldData();
                                soldData.setKey(soldData.getKey().add(price));
                                soldData.setValue(soldData.getValue() + drop.getAmount());
                            }
                        }
                    });
                }

                // Handle auto pickup
                if (prisoner.isAutoPickup()) {
                    List<BackPack> backpacks = SuperiorPrisonPlugin.getInstance().getBackPackController().findBackPacks(player);
                    for (BackPack backpack : backpacks) {
                        Map<ItemStack, Integer> add = backpack.add(drops.toArray(new ItemStack[0]));

                        Runnable modify = () -> {
                            if (!backpack.isModified()) return;
                            backpack.save();
                            backpack.update();
                        };

                        if (add.isEmpty()) {
                            drops.clear();
                            modify.run();
                            break;

                        } else {
                            modify.run();
                            drops.clear();
                            drops.addAll(add.keySet());
                        }
                    }

                    if (!drops.isEmpty()) {
                        HashMap<Integer, ItemStack> left = player.getInventory().addItem(drops.toArray(new ItemStack[0]));
                        if (left.isEmpty())
                            drops.clear();

                        else {
                            drops.clear();
                            drops.addAll(left.values());

                            LocaleEnum.AUTO_PICKUP_PRISONER_INVENTORY_FULL.getWithErrorPrefix().send(player);
                        }
                    }
                }

                if (!player.hasPermission("prison.prisoner.ignoredurability")) {
                    int enchantmentLevel = tool.getEnchantLevel(Enchantment.DURABILITY);
                    if (enchantmentLevel != 0) {
                        double chance = (100 / enchantmentLevel + 1);
                        double generatedChance = ThreadLocalRandom.current().nextDouble(0, 100);

                        if (chance > generatedChance)
                            tool.setDurability((short) (tool.getDurability() + 1));
                    } else
                        tool.setDurability((short) (tool.getDurability() + 1));

                    if (tool.getDurability() == tool.getMaterial().getMaxDurability()) {
                        player.setItemInHand(null);
                    }
                } else
                    tool.setDurability(0);

                drops.forEach(item -> event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0, 0.5), item));

                event.getBlock().getDrops().clear();
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
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

        SyncEvents.listen(AsyncPlayerChatEvent.class, EventPriority.HIGHEST, event -> {
            if (!SuperiorPrisonPlugin.getInstance().getChatController().isEnabled()) return;
            if (event.isCancelled()) return;

            ChatFormat chatFormat = SuperiorPrisonPlugin.getInstance().getChatController().findHighest(SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer()));
            if (chatFormat == null) return;

            event.setCancelled(true);

            OMessage clone = chatFormat.getFormat().clone();
            SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> PapiHook.class, hook -> clone.replace(in -> hook.parse(event.getPlayer(), (String) in)));

            clone.replace("%message%", ChatColor.stripColor(Helper.color(event.getMessage())));
            clone.send(Helper.getOnlinePlayers().toArray(new Player[0]));
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
