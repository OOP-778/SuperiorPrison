package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.mine.locks.Lock;
import com.bgsoftware.superiorprison.api.event.mine.MultiBlockBreakEvent;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.bomb.BombConfig;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.controller.BombController;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.locks.SBLocksLock;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.*;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.particle.OParticle;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;
import static com.oop.orangeengine.main.events.AsyncEvents.async;

public class BombListener {
    public BombListener() {
        BombController controller = SuperiorPrisonPlugin.getInstance().getBombController();

        SyncEvents.listen(PlayerInteractEvent.class, event -> {
            Optional<BombConfig> bombOf = controller.getBombOf(event.getPlayer().getItemInHand());
            if (!bombOf.isPresent()) return;

            if (event.getClickedBlock() != null) {
                event.setCancelled(true);
                return;
            }

            SPrisoner insertIfAbsent = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer());
            Optional<Pair<SuperiorMine, AreaEnum>> optCurrentMine = insertIfAbsent.getCurrentMine();
            if (!optCurrentMine.isPresent()) return;

            Pair<SuperiorMine, AreaEnum> mine = optCurrentMine.get();
            if (mine.getValue() == AreaEnum.REGION) {
                LocaleEnum
                        .BOMB_CAN_ONLY_BE_USED_INSIDE_MINE
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

            BlockFace direction = Directional.getDirection(event.getPlayer());
            BombConfig bomb = bombOf.get();

            long cooldown = controller.getCooldown(event.getPlayer(), bomb);
            if (cooldown != -1) {
                if (cooldown > System.currentTimeMillis()) {
                    messageBuilder(LocaleEnum.BOMB_STILL_ON_COOLDOWN.getWithErrorPrefix())
                            .replace("{cooldown}", TimeUtil.toString(TimeUnit.MILLISECONDS.toSeconds(cooldown - System.currentTimeMillis())))
                            .send(event.getPlayer());
                    return;
                }
                controller.removeCooldown(event.getPlayer(), bomb);
            }
            ItemStack clone = event.getPlayer().getItemInHand().clone();
            clone.setAmount(1);

            event.getPlayer().getInventory().removeItem(clone);

            ArmorStand a = event.getPlayer().getWorld().spawn(event.getPlayer().getEyeLocation().add(0, -1, 0), ArmorStand.class);

            a.setGravity(true);
            a.setVelocity(event.getPlayer().getLocation().getDirection().multiply(2.8));
            a.setVisible(false);
            a.setSmall(true);
            a.setHelmet(bomb.getItem());

            new OTask()
                    .sync(false)
                    .repeat(true)
                    .delay(TimeUnit.MILLISECONDS, 20)
                    .consumer(task -> {
                        Location checkLocation = a.getEyeLocation().clone().add(direction.getModX(), direction.getModY(), direction.getModZ());

                        Vector velocity = a.getVelocity();
                        double sum = Math.abs(velocity.getX()) + Math.abs(velocity.getY()) + Math.abs(velocity.getZ());

                        if (sum < 0.75 || checkLocation.getY() < 0) {
                            task.cancel();
                            StaticTask.getInstance().sync(a::remove);

                            Area area = mine.getKey().getArea(checkLocation);
                            if (area == null || area.getType() != AreaEnum.MINE) {
                                LocaleEnum.BOMB_FAILED_TO_LAND_IN_MINE.getWithErrorPrefix().send(event.getPlayer());
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

                            controller.putCooldown(event.getPlayer(), bomb);

                            SNormalMine key = (SNormalMine) mine.getKey();
                            Lock lock = key.getGenerator().getBlockData().newBlockDataLock();
                            try {
                                List<Location> sphereAt = key.getGenerator().getCuboid().getSphereAt(checkLocation, bomb.getRadius());

                                AtomicInteger counter = new AtomicInteger();
                                Set<ChunkResetData> data = new HashSet<>();

                                ThreadLocalRandom random = ThreadLocalRandom.current();
                                Consumer<Location> particleExecution = loc -> {
                                    if (bomb.getExplosionParticle() == null) return;

                                    if (bomb.getParticleShownAt() == -1)
                                        OParticle.getProvider().display(bomb.getExplosionParticle(), loc, 1);
                                    else if (random.nextDouble(1) < (bomb.getParticleShownAt() / 100.0)) {
                                        OParticle.getProvider().display(bomb.getExplosionParticle(), loc, 1);
                                    }
                                };

                                long dropStart = System.currentTimeMillis();
                                SuperiorPrisonPlugin.getInstance().getBlockController().handleBlockBreak(
                                        SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer()),
                                        mine.getKey(),
                                        null,
                                        lock,
                                        sphereAt.toArray(new Location[0])
                                );
                                ClassDebugger.debug("Took {}ms", (System.currentTimeMillis() - dropStart));

                                int[] amount = new int[]{0};
                                for (Location location : sphereAt) {
                                    if (!mine.getKey().getGenerator().getBlockData().has(location))
                                        continue;

                                    ((SBLocksLock) lock).getLockedLocations().add(location);
                                    ClassDebugger.debug("Setting block");

                                    amount[0] = amount[0] + 1;
                                    data.add(
                                            SuperiorPrisonPlugin.getInstance().getMineController().addResetBlock(location, OMaterial.AIR, () -> {
                                                particleExecution.accept(location);
                                                if (counter.incrementAndGet() == amount[0]) {
                                                    async(() -> SuperiorPrisonPlugin.getInstance().getNms().refreshChunks(checkLocation.getWorld(), sphereAt, checkLocation.getWorld().getPlayers()));
                                                    key.getGenerator().getBlockData().unlock(lock);
                                                }
                                            })
                                    );
                                }

                                for (ChunkResetData datum : data)
                                    datum.setReady(true);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                key.getGenerator().getBlockData().unlock(lock);
                            }
                        } else if (bomb.getTrailParticle() != null)
                            OParticle.getProvider().display(bomb.getTrailParticle(), checkLocation.clone().add(0, 0.2, 0.0), 1);
                    })
                    .execute();
        });
    }
}
