package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.bomb.BombConfig;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.controller.BombController;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
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
            if (event.getAction().name().contains("AIR")) {
                SPrisoner insertIfAbsent = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer());
                Optional<Pair<SuperiorMine, AreaEnum>> optCurrentMine = insertIfAbsent.getCurrentMine();
                if (!optCurrentMine.isPresent()) return;

                Optional<BombConfig> bombOf = controller.getBombOf(event.getPlayer().getItemInHand());
                if (!bombOf.isPresent()) return;

                Pair<SuperiorMine, AreaEnum> mine = optCurrentMine.get();
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

                ArmorStand a = event.getPlayer().getWorld().spawn(event.getPlayer().getEyeLocation().add(0, -1, 0), ArmorStand.class);

                a.setGravity(true);
                a.setVelocity(event.getPlayer().getLocation().getDirection().multiply(2.5));
                a.setVisible(false);
                a.setSmall(true);
                a.setHelmet(bomb.getItem());

                new OTask()
                        .sync(false)
                        .repeat(true)
                        .delay(TimeUnit.MILLISECONDS, 50)
                        .consumer(task -> {
                            Location checkLocation = a.getEyeLocation().clone().add(direction.getModX(), direction.getModY(), direction.getModZ());

                            Vector velocity = a.getVelocity();
                            double sum = Math.abs(velocity.getX()) + Math.abs(velocity.getY()) + Math.abs(velocity.getZ());

                            if (sum < 0.85 || checkLocation.getY() < 0) {
                                task.cancel();
                                StaticTask.getInstance().sync(a::remove);

                                System.out.println("Doing 2");
                                Area area = mine.getKey().getArea(checkLocation);
                                if (area == null || area.getType() != AreaEnum.MINE) {
                                    LocaleEnum.BOMB_FAILED_TO_LAND_IN_MINE.getWithErrorPrefix().send(event.getPlayer());
                                    event.getPlayer().getInventory().addItem(bomb.getItem());
                                    return;
                                }

                                controller.putCooldown(event.getPlayer(), bomb);

                                System.out.println("Doing 3");

                                SNormalMine key = (SNormalMine) mine.getKey();
                                System.out.println("boom");
                                try {
                                    long start = System.currentTimeMillis();
                                    List<Location> sphereAt = key.getGenerator().getCuboid().getSphereAt(checkLocation, bomb.getRadius());
                                    long end = (System.currentTimeMillis() - start);
                                    System.out.println("Location Gathering done. Took " + end + "ms");

                                    System.out.println("blocks at sphere: " + sphereAt.size());
                                    AtomicInteger counter = new AtomicInteger();
                                    Set<ChunkResetData> data = new HashSet<>();

                                    ThreadLocalRandom random = ThreadLocalRandom.current();
                                    Consumer<Location> particleExecution = loc -> {
                                        if (bomb.getExplosionParticle() == null) return;

                                        if (bomb.getParticleShownAt() == -1)
                                            OParticle.getProvider().display(bomb.getExplosionParticle(), loc, 1);
                                        else if (random.nextDouble() < (bomb.getParticleShownAt() / 100.0)) {
                                            OParticle.getProvider().display(bomb.getExplosionParticle(), loc, 1);
                                        }
                                    };

                                    long dropStart = System.currentTimeMillis();
                                    SuperiorPrisonPlugin.getInstance().getBlockController().syncHandleBlockBreak(
                                            SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer()),
                                            mine.getKey(),
                                            null,
                                            sphereAt.toArray(new Location[0])
                                    );
                                    ClassDebugger.debug("Took {}ms", (System.currentTimeMillis() - dropStart));

                                    for (Location location : sphereAt) {
                                        data.add(
                                                SuperiorPrisonPlugin.getInstance().getMineController().addResetBlock(location, OMaterial.AIR, () -> {
                                                    particleExecution.accept(location);
                                                    if (counter.incrementAndGet() == sphereAt.size()) {
                                                        System.out.println("done");
                                                        async(() -> SuperiorPrisonPlugin.getInstance().getNms().refreshChunks(checkLocation.getWorld(), sphereAt, checkLocation.getWorld().getPlayers()));
                                                    }
                                                })
                                        );
                                    }

                                    for (ChunkResetData datum : data) {
                                        datum.setReady(true);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else if (bomb.getTrailParticle() != null)
                                OParticle.getProvider().display(bomb.getTrailParticle(), checkLocation.clone().add(0, 0.3, 0.0), 1);
                        })
                        .execute();
            }
        });
    }
}
