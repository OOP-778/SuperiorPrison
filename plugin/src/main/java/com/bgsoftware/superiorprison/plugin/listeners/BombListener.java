package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.mine.locks.Lock;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.bomb.BombConfig;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.controller.BombController;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.ClassDebugger;
import com.bgsoftware.superiorprison.plugin.util.Directional;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.cache.OCache;
import com.oop.orangeengine.particle.OParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class BombListener {
  private final OCache<String, Boolean> handledEventCache =
      OCache.builder().concurrencyLevel(1).expireAfter(300).build();

  private final BombController controller = SuperiorPrisonPlugin.getInstance().getBombController();

  public BombListener() {
    SyncEvents.listen(
        PlayerInteractEvent.class,
        event -> {
          if (event.getItem() == null || event.getItem().getType() == Material.AIR) return;
          if (handledEventCache.get(event.getPlayer().getName()) != null) return;

          Optional<BombConfig> bombOf = controller.getBombOf(event.getItem());
          if (!bombOf.isPresent()) return;
          event.setCancelled(true);

          SPrisoner insertIfAbsent =
              SuperiorPrisonPlugin.getInstance()
                  .getPrisonerController()
                  .getInsertIfAbsent(event.getPlayer());
          Optional<Pair<SuperiorMine, AreaEnum>> optCurrentMine = insertIfAbsent.getCurrentMine();
          if (!optCurrentMine.isPresent()) return;

          Pair<SuperiorMine, AreaEnum> mine = optCurrentMine.get();
          if (mine.getValue() == AreaEnum.REGION) {
            LocaleEnum.BOMB_CAN_ONLY_BE_USED_INSIDE_MINE
                .getWithErrorPrefix()
                .send(event.getPlayer());
            return;
          }

          if (!mine.getKey().isReady()) {
            LocaleEnum.CANCELED_ACTION_CAUSE_MINE_RESET
                .getWithErrorPrefix()
                .send(event.getPlayer());
            return;
          }

          BombConfig bomb = bombOf.get();
          long cooldown = controller.getCooldown(event.getPlayer(), bomb);
          if (cooldown != -1) {
            if (cooldown > System.currentTimeMillis()) {
              messageBuilder(LocaleEnum.BOMB_STILL_ON_COOLDOWN.getWithErrorPrefix())
                  .replace(
                      "{cooldown}",
                      TimeUtil.toString(
                          TimeUnit.MILLISECONDS.toSeconds(cooldown - System.currentTimeMillis())))
                  .send(event.getPlayer());
              return;
            }
            controller.removeCooldown(event.getPlayer(), bomb);
          }

          handledEventCache.put(event.getPlayer().getName(), true);
          if (event.getClickedBlock() != null) {
            if (!mine.getKey()
                .getArea(AreaEnum.MINE)
                .isInsideWithoutY(event.getClickedBlock().getLocation())) {
              return;
            }

            ItemStack clone = event.getItem().clone();
            clone.setAmount(1);

            event.getPlayer().getInventory().removeItem(clone);
            boomAt(
                (SNormalMine) mine.getKey(),
                event.getPlayer(),
                event.getClickedBlock().getLocation(),
                bomb);
            return;
          }

          BlockFace direction = Directional.getDirection(event.getPlayer());
          ItemStack clone = event.getItem().clone();
          clone.setAmount(1);

          event.getPlayer().getInventory().removeItem(clone);

          ArmorStand a =
              event
                  .getPlayer()
                  .getWorld()
                  .spawn(event.getPlayer().getEyeLocation().add(0, -1, 0), ArmorStand.class);

          a.setGravity(true);
          a.setVelocity(event.getPlayer().getLocation().getDirection().multiply(2.8));
          a.setVisible(false);
          a.setSmall(true);
          a.setHelmet(bomb.getItem());

          new OTask()
              .sync(false)
              .repeat(true)
              .delay(TimeUnit.MILLISECONDS, 20)
              .consumer(
                  task -> {
                    Location checkLocation =
                        a.getEyeLocation()
                            .clone()
                            .add(direction.getModX(), direction.getModY(), direction.getModZ());

                    Vector velocity = a.getVelocity();
                    double sum =
                        Math.abs(velocity.getX())
                            + Math.abs(velocity.getY())
                            + Math.abs(velocity.getZ());

                    if (sum < 0.75 || checkLocation.getY() < 0) {
                      task.cancel();
                      StaticTask.getInstance().sync(a::remove);

                      Area area = mine.getKey().getArea(checkLocation);
                      if (area == null || area.getType() != AreaEnum.MINE) {
                        LocaleEnum.BOMB_FAILED_TO_LAND_IN_MINE
                            .getWithErrorPrefix()
                            .send(event.getPlayer());
                        event.getPlayer().getInventory().addItem(bomb.getItem());
                        return;
                      }

                      if (!mine.getKey().isReady()) {
                        LocaleEnum.CANCELED_ACTION_CAUSE_MINE_RESET
                            .getWithErrorPrefix()
                            .send(event.getPlayer());
                        event.getPlayer().getInventory().addItem(bomb.getItem());
                        return;
                      }

                      boomAt((SNormalMine) mine.getKey(), event.getPlayer(), checkLocation, bomb);
                    } else if (bomb.getTrailParticle() != null)
                      OParticle.getProvider()
                          .display(
                              bomb.getTrailParticle(), checkLocation.clone().add(0, 0.2, 0.0), 1);
                  })
              .execute();
        });
  }

  public void boomAt(SNormalMine mine, Player player, Location boomAt, BombConfig bomb) {
    controller.putCooldown(player, bomb);

    Lock lock = mine.newLock();
    try {
      Set<Location> sphereAt =
          mine.getGenerator().getCuboid().getSphereAt(boomAt, bomb.getRadius());

      ThreadLocalRandom random = ThreadLocalRandom.current();
      Consumer<Location> particleExecution =
          loc -> {
            if (bomb.getExplosionParticle() == null) return;

            if (bomb.getParticleShownAt() == -1)
              OParticle.getProvider().display(bomb.getExplosionParticle(), loc, 1);
            else if (random.nextDouble(1) < (bomb.getParticleShownAt() / 100.0)) {
              OParticle.getProvider().display(bomb.getExplosionParticle(), loc, 1);
            }
          };

      long boomStart = System.currentTimeMillis();
      StaticTask.getInstance()
          .ensureSync(
              () -> {
                SuperiorPrisonPlugin.getInstance()
                    .getBlockController()
                    .breakBlock(
                        SuperiorPrisonPlugin.getInstance()
                            .getPrisonerController()
                            .getInsertIfAbsent(player),
                        mine,
                        null,
                        sphereAt.toArray(new Location[0]));
              });
      ClassDebugger.debug("Bomb Execution Took {}ms", (System.currentTimeMillis() - boomStart));

      StaticTask.getInstance()
          .async(
              () -> {
                for (Location location : sphereAt) {
                  if (!mine.getGenerator().getBlockData().has(location)) continue;

                  particleExecution.accept(location);
                }
              });

    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      mine.unlock(lock);
    }
  }
}
