package com.bgsoftware.superiorprison.plugin.listeners;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.ChunkResetData;
import com.bgsoftware.superiorprison.plugin.util.Directional;
import com.bgsoftware.superiorprison.plugin.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.util.input.Input;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.particle.OParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Egg;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.bgsoftware.superiorprison.plugin.util.Hitter.listenForHit;
import static com.oop.orangeengine.main.events.AsyncEvents.async;

public class BombListener {
    public BombListener() {
        SyncEvents.listen(PlayerInteractEvent.class, event -> {
            if (event.getAction().name().contains("AIR")) {

                SPrisoner insertIfAbsent = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer());
                Optional<Pair<SuperiorMine, AreaEnum>> optCurrentMine = insertIfAbsent.getCurrentMine();
                if (!optCurrentMine.isPresent()) return;

                Pair<SuperiorMine, AreaEnum> mine = optCurrentMine.get();

                Egg egg = event.getPlayer().getWorld().spawn(event.getPlayer().getLocation().clone().add(0.5, -0.8, 0.5), Egg.class);
                egg.setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.0));
                listenForHit(egg, () -> {
                    final Location[] hitLocation = {egg.getLocation()};
                    OParticle.getProvider().display("CLOUD", hitLocation[0], 10);
                    egg.remove();

                    event.getPlayer().sendMessage("Type in the radius of bomb");
                    Input
                            .integerInput(event.getPlayer())
                            .onInput((i, radius) -> {
                                i.cancel();

                                BlockFace direction = Directional.fromDirection(event.getPlayer().getEyeLocation().getYaw());
                                hitLocation[0] = hitLocation[0].add(direction.getModX() * (radius - 1), direction.getModY() * (radius - 1), direction.getModZ() * (radius - 1));

                                Location finalHitLocation = hitLocation[0];
                                async(() -> {
                                    System.out.println("Doing 2");
                                    if (mine.getKey().getArea(finalHitLocation).getType() != AreaEnum.MINE) return;
                                    System.out.println("Doing 3");

                                    SNormalMine key = (SNormalMine) mine.getKey();
                                    System.out.println("boom");
                                    try {
                                        long start = System.currentTimeMillis();
                                        Set<SPLocation> sphereAt = key.getGenerator().getCuboid().getSphereAt(finalHitLocation, radius);
                                        long end = (System.currentTimeMillis() - start);
                                        System.out.println("Location Gathering done. Took " + end + "ms");

                                        System.out.println("blocks at sphere: " + sphereAt.size());
                                        AtomicInteger counter = new AtomicInteger();
                                        Set<ChunkResetData> data = new HashSet<>();
                                        for (SPLocation location : sphereAt)
                                            data.add(
                                                    SuperiorPrisonPlugin.getInstance().getMineController().addResetBlock(location.toBukkit(), OMaterial.AIR, () -> {
                                                        OParticle.getProvider().display("EXPLOSION_LARGE", location.toBukkit(), 2);
                                                        if (counter.incrementAndGet() == sphereAt.size()) {
                                                            System.out.println("done");
                                                            SuperiorPrisonPlugin.getInstance().getNms().refreshChunks(finalHitLocation.getWorld(), sphereAt, finalHitLocation.getWorld().getPlayers());
                                                        }
                                                    })
                                            );

                                        for (ChunkResetData datum : data) {
                                            datum.setReady(true);
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                            })
                            .listen();
                });
            }
        });
    }
}
