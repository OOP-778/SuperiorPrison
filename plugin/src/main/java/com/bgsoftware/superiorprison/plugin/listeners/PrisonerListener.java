package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.event.mine.MineEnterEvent;
import com.bgsoftware.superiorprison.api.event.mine.MineLeaveEvent;
import com.bgsoftware.superiorprison.api.event.mine.MultiBlockBreakEvent;
import com.bgsoftware.superiorprison.api.event.mine.area.MineAreaChangeEvent;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.Updater;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.bgsoftware.superiorprison.plugin.object.chat.ChatFormat;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.mine.effects.SMineEffect;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;
import java.util.Set;
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
                    .delay(100)
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

        SyncEvents.listen(PlayerKickEvent.class, event -> handleLeave(event.getPlayer()));

        SyncEvents.listen(PlayerQuitEvent.class, event -> handleLeave(event.getPlayer()));

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

            // Check when prisoner mines in a different area
            if (minePair.getKey().getArea(event.getBlock().getLocation()).getType() != area.getType()) {
                event.setCancelled(true);
                return;
            }

            // Check if prisoner is breaking under the mine
            if (!area.isInsideWithY(new SPLocation(event.getBlock().getLocation()), true)) {
                event.setCancelled(true);
                return;
            }

            if (!minePair.getKey().isReady()) {
                LocaleEnum.CANCELED_ACTION_CAUSE_MINE_RESET
                        .getWithErrorPrefix()
                        .send(event.getPlayer());
                event.setCancelled(true);
                return;
            }

            if (minePair.getKey().getGenerator().getBlockData().isLocked(event.getBlock().getLocation())) {
                LocaleEnum
                        .BLOCK_CANNOT_MINE_LOCKED
                        .getWithErrorPrefix()
                        .send(prisoner.getPlayer());
                event.setCancelled(true);
                return;
            }

            OMaterial blockMaterial = OMaterial.matchMaterial(event.getBlock());

            BlockBreakEvent bouncedEvent = new BlockBreakEvent(event.getBlock(), prisoner.getPlayer());
            ignoreEvents.put(bouncedEvent, true);

            Bukkit.getPluginManager().callEvent(bouncedEvent);
            ignoreEvents.remove(bouncedEvent);

            if (bouncedEvent.isCancelled()) {
                event.setExpToDrop(0);
                event.setCancelled(true);
            }

            // If they not cancelled it, but set the block to air. Same...
            if (bouncedEvent.isCancelled() || event.getBlock().getType() == Material.AIR) {
                minePair.getKey().getGenerator().getBlockData().remove(event.getBlock().getLocation());
                SuperiorPrisonPlugin.getInstance().getStatisticsController().getContainer(prisoner.getUUID()).getBlocksStatistic().update(blockMaterial, 1);
                return;
            }

            SuperiorPrisonPlugin.getInstance().getBlockController().breakBlock(
                    prisoner,
                    minePair.getKey(),
                    event.getPlayer().getItemInHand(),
                    event.getBlock().getLocation()
            );
        });

        SyncEvents.listen(MineEnterEvent.class, EventPriority.LOWEST, event -> {
            if (!event.getMine().canEnter(event.getPrisoner())) {
                event.setCancelled(true);
                return;
            }

            event.getMine().getEffects().get().stream().map(effect -> (SMineEffect) effect).map(SMineEffect::create).forEach(effect -> event.getPrisoner().getPlayer().addPotionEffect(effect, false));
            ((SPrisoner) event.getPrisoner()).setLogoutMine(event.getMine().getName());
        });

        SyncEvents.listen(MineAreaChangeEvent.class, EventPriority.LOWEST, event -> {
            if (event.getMine().getGenerator().isCaching() || event.getMine().getGenerator().isResetting()) {
                event.setCancelled(true);
                LocaleEnum.CANNOT_ENTER_MINE_MINE_NOT_READY.getWithPrefix().send(event.getPrisoner().getPlayer());
            }
        });

        SyncEvents.listen(MineLeaveEvent.class, event -> {
            ((SPrisoner) event.getPrisoner()).setLogoutMine(null);
            event.getPrisoner().save(true);
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

    public void handleLeave(Player player) {
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getPrisonerHolder().getPrisoner(player.getUniqueId()).map(prisoner -> (SPrisoner) prisoner).ifPresent(prisoner -> {
            prisoner.getCurrentMine().ifPresent(mine -> {
                prisoner.setLogoutMine(mine.getKey().getName());

                prisoner.getCurrentMine().get().getKey().getPrisoners().remove(prisoner);
                prisoner.setCurrentMine(null);
                prisoner.save(false);
            });

            prisoner.clearCache();
        });
    }
}
