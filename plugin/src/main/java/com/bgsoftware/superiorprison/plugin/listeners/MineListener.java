package com.bgsoftware.superiorprison.plugin.listeners;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;
import static com.oop.orangeengine.main.events.AsyncEvents.async;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.mine.settings.MineSettings;
import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.event.mine.MineBlockBreakEvent;
import com.bgsoftware.superiorprison.api.event.mine.MineEnterEvent;
import com.bgsoftware.superiorprison.api.event.mine.MineLeaveEvent;
import com.bgsoftware.superiorprison.api.event.mine.MultiBlockBreakEvent;
import com.bgsoftware.superiorprison.api.event.mine.area.MineAreaChangeEvent;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SResetSettings;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.SPair;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
import com.oop.orangeengine.main.events.SyncEvents;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class MineListener {
  public MineListener() {
    SMineHolder mineHolder =
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getMineHolder();
    SPrisonerHolder prisonerHolder =
        SuperiorPrisonPlugin.getInstance().getDatabaseController().getPrisonerHolder();

    // Disallow block place event if no perm
    SyncEvents.listen(
        MineBlockBreakEvent.class,
        EventPriority.LOWEST,
        event -> {
          if (event.isCancelled()) return;
          Player player = event.getPrisoner().getPlayer();

          // World check if should make it a bit lighter
          if (!mineHolder.getMinesWorlds().contains(player.getLocation().getWorld().getName()))
            return;

          // If prisoner isn't in a mine return
          Prisoner prisoner = prisonerHolder.getInsertIfAbsent(player);
          if (!prisoner.getCurrentMine().isPresent()) return;

          SNormalMine superiorMine = (SNormalMine) prisoner.getCurrentMine().get().getKey();
          AreaEnum areaTypeAt = superiorMine.getAreaTypeAt(event.getBlock().getLocation());

          if (areaTypeAt == AreaEnum.MINE && !superiorMine.getGenerator().isCaching()) {
            superiorMine.getGenerator().getBlockData().remove(event.getBlock().getLocation());
            superiorMine.save(true);

            if (superiorMine.getSettings().getResetSettings().isTimed()) return;
            async(
                () -> {
                  int percentageOfFullBlocks =
                      superiorMine.getGenerator().getBlockData().getPercentageLeft();
                  long percentageRequired =
                      superiorMine.getSettings().getResetSettings().asPercentage().getValue();

                  if (percentageOfFullBlocks <= percentageRequired) {
                    superiorMine.getGenerator().reset();
                  }
                });
          }
        });

    // Mine Leave & Enter events handling
    SyncEvents.listen(
        PlayerMoveEvent.class,
        event -> {
          if (event.getPlayer().hasMetadata("NPC")) return;

          // Checks if the player actually moved a block.
          Location from = event.getFrom(), to = event.getTo();
          if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) return;

          // World check
          Set<String> worldNames = mineHolder.getMinesWorlds();
          if (!worldNames.contains(event.getPlayer().getWorld().getName())) return;

          // Get prisoner
          SPrisoner prisoner = prisonerHolder.getInsertIfAbsent(event.getPlayer());
          Optional<Pair<SuperiorMine, AreaEnum>> mineOptional = prisoner.getCurrentMine();

          if (mineOptional.isPresent()) {
            SPair<SuperiorMine, AreaEnum> mine = (SPair<SuperiorMine, AreaEnum>) mineOptional.get();
            AreaEnum currentAt = mine.getKey().getAreaTypeAt(event.getTo());
            if (currentAt == null) {

              MineLeaveEvent leaveEvent = new MineLeaveEvent(mine.getKey(), prisoner, null);
              Bukkit.getPluginManager().callEvent(leaveEvent);

              mine.getKey().getPrisoners().remove(prisoner);
              prisoner.setCurrentMine(null);

            } else if (currentAt != mine.getValue()) {
              MineAreaChangeEvent areaChangeEvent =
                  new MineAreaChangeEvent(
                      prisoner,
                      mine.getKey().getAreaTypeAt(event.getFrom()),
                      currentAt,
                      mine.getKey());
              Bukkit.getPluginManager().callEvent(areaChangeEvent);

              if (areaChangeEvent.isCancelled()) {
                Framework.FRAMEWORK.teleport(event.getPlayer(), mine.getKey().getSpawnPoint());
                return;
              }

              mine.setValue(currentAt);
            }

          } else {
            Optional<SuperiorMine> mineAt =
                SuperiorPrisonPlugin.getInstance().getMineController().getMineAt(event.getTo());
            if (mineAt.isPresent()) {
              SuperiorMine mine = mineAt.get();

              MineEnterEvent enterEvent =
                  new MineEnterEvent(mine, prisoner, mine.getArea(event.getTo()));
              Bukkit.getPluginManager().callEvent(enterEvent);

              if (enterEvent.isCancelled()) {
                Framework.FRAMEWORK.teleport(
                    event.getPlayer(), event.getPlayer().getWorld().getSpawnLocation());
                return;
              }

              mine.getPrisoners().add(prisoner);
              prisoner.setCurrentMine(new SPair<>(mine, mine.getAreaTypeAt(event.getTo())));
            }
          }
        });

    SyncEvents.listen(
        PlayerTeleportEvent.class,
        event -> {
          if (event.getPlayer().hasMetadata("NPC")) return;

          // World check
          Set<String> worldNames = mineHolder.getMinesWorlds();
          if (!worldNames.contains(event.getFrom().getWorld().getName())
              && !worldNames.contains(event.getTo().getWorld().getName())) return;

          // Get prisoner
          SPrisoner prisoner = prisonerHolder.getInsertIfAbsent(event.getPlayer());

          // Check if prisoner is leaving mine
          prisoner
              .getCurrentMine()
              .ifPresent(
                  mine -> {
                    if (mine.getKey().isInside(event.getTo())) return;

                    MineLeaveEvent leaveEvent = new MineLeaveEvent(mine.getKey(), prisoner, null);
                    Bukkit.getPluginManager().callEvent(leaveEvent);

                    if (leaveEvent.isCancelled()) {
                      event.setCancelled(true);

                    } else {
                      mine.getKey().getPrisoners().remove(prisoner);
                      prisoner.setCurrentMine(null);
                    }
                  });

          // Checking if mine exists at event#getTo()
          SuperiorPrisonPlugin.getInstance()
              .getMineController()
              .getMineAt(event.getTo())
              .ifPresent(
                  mine -> {
                    if (!mine.canEnter(prisoner)) {
                      return;
                    }

                    MineEnterEvent enterEvent =
                        new MineEnterEvent(mine, prisoner, mine.getArea(event.getTo()));
                    Bukkit.getPluginManager().callEvent(enterEvent);

                    if (enterEvent.isCancelled()) {
                      event.setCancelled(true);
                      return;
                    }

                    mine.getPrisoners().add(prisoner);
                    prisoner.setCurrentMine(new SPair<>(mine, mine.getAreaTypeAt(event.getTo())));
                  });
        });

    SyncEvents.listen(
        MineEnterEvent.class,
        EventPriority.LOWEST,
        event -> {
          if (!event.getMine().canEnter(event.getPrisoner())) {
            event.setCancelled(true);
            messageBuilder(LocaleEnum.CANNOT_ENTER_MINE_MISSING_RANK.getWithErrorPrefix())
                .replace(
                    "{rank}",
                    event.getMine().getRanksMapped().stream()
                        .filter(rank -> rank instanceof LadderRank)
                        .map(rank -> (LadderRank) rank)
                        .min(Comparator.comparingInt(LadderRank::getOrder))
                        .map(Rank::getName)
                        .orElse("None"))
                .send(event.getPrisoner().getPlayer());
          }
        });

    SyncEvents.listen(
        CreatureSpawnEvent.class,
        EventPriority.LOWEST,
        event -> {
          if (event.getEntity() instanceof Player) return;
          if (event.getEntity().hasMetadata("NPC")) return;

          // World check
          Set<String> worldNames = mineHolder.getMinesWorlds();
          if (!worldNames.contains(event.getEntity().getWorld().getName())) return;

          Optional<SuperiorMine> mineAt = mineHolder.getMineAt(event.getLocation());
          if (!mineAt.isPresent()) return;

          MineSettings settings = mineAt.get().getSettings();
          if (event.getEntity() instanceof Monster && settings.isDisableMonsterSpawn()) {
            event.setCancelled(true);
          } else if (event.getEntity() instanceof Animals && settings.isDisableAnimalSpawn()) {
            event.setCancelled(true);
          }
        });

    SyncEvents.listen(
        PlayerDropItemEvent.class,
        event -> {
          // World check
          Set<String> worldNames = mineHolder.getMinesWorlds();
          if (!worldNames.contains(event.getItemDrop().getWorld().getName())) return;

          Optional<SuperiorMine> mineAt = mineHolder.getMineAt(event.getItemDrop().getLocation());
          if (!mineAt.isPresent()) return;

          if (!SuperiorPrisonPlugin.getInstance().getMainConfig().isItemDropping()) {
            event.setCancelled(true);
          }
        });

    SyncEvents.listen(
        MultiBlockBreakEvent.class,
        EventPriority.LOWEST,
        event -> {
          if (event.getMine().getSettings().getResetSettings()
              instanceof SResetSettings.SPercentage) {
            ResetSettings.Percentage percentage =
                event.getMine().getSettings().getResetSettings().asPercentage();
            int percentageLeft = event.getMine().getGenerator().getBlockData().getPercentageLeft();
            if (percentageLeft <= percentage.getValue()) event.getMine().getGenerator().reset();
          }
        });
  }
}
