package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.IMineGenerator;
import com.bgsoftware.superiorprison.api.data.mine.ISuperiorMine;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.bgsoftware.superiorprison.plugin.util.Cuboid;
import com.bgsoftware.superiorprison.plugin.util.ReflectionUtils;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.world.WorldLoadEvent;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Setter
@Getter
public class MineGenerator implements IMineGenerator, Serializable, Attachable<ISuperiorMine> {

    private transient ISuperiorMine mine;
    private List<OPair<Double, Material>> generatorMaterials = new ArrayList<>();
    private transient Instant lastReset;
    private transient Instant nextReset;

    // << CACHING >>
    private transient Block[] cachedMineArea = new Block[]{};
    private transient boolean caching = false;
    private transient boolean worldLoadWait = false;

    private Material[] cachedMaterials = new Material[]{};
    private transient boolean materialsChanged = false;

    public MineGenerator() {
    }

    @Override
    public Instant getLastReset() {
        return lastReset;
    }

    @Override
    public Instant getNextReset() {
        return nextReset;
    }

    public void generate() {
        if (cachedMineArea.length == 0) return;

        int blocksInRegion = cachedMineArea.length;
        if (cachedMaterials.length == 0 || materialsChanged) {

            cachedMaterials = new Material[blocksInRegion];
            for (OPair<Double, Material> generatorMaterial : generatorMaterials) {
                int amount = (int) ((generatorMaterial.getFirst() / 100d) * blocksInRegion);
                for (int i = 0; i < amount; i++)
                    cachedMaterials[i] = generatorMaterial.getSecond();

            }
        }

        cachedMaterials = shuffleArray(cachedMaterials);

        for (int index = 0; index < blocksInRegion; index++) {

            Block block = cachedMineArea[index];
            Material material = cachedMaterials[index];

            StaticTask.getInstance().gatherFromSync(() -> block.getType() == Material.AIR).whenComplete((bool, thrw) -> {
                if (bool) return;

                ReflectionUtils.setBlock(block.getLocation(), material);

            });
        }

        for (Block block : cachedMineArea) {
            AtomicBoolean set = new AtomicBoolean(false);

            generatorMaterials.forEach(pair -> {
                if (set.get()) return;

                CompletableFuture<Boolean> isAir = StaticTask.getInstance().gatherFromSync(() -> block.getType() == Material.AIR);
                isAir.whenComplete((bool, thrw) -> {
                    if (!bool) return;

                    double chance = ThreadLocalRandom.current().nextDouble(0, 1);
                    double materialChance = pair.getFirst() / 100;

                    if (chance <= materialChance) {
                        ReflectionUtils.setBlock(block.getLocation(), pair.getSecond());
                        set.set(true);
                    }

                });
            });
        }

    }

    public void initCache() {
        if (isCaching() || isWorldLoadWait())
            return;

        if (mine.getMinPoint().getWorld() == null) {
            worldLoadWait = true;
            SubscriptionFactory.getInstance().subscribeTo(WorldLoadEvent.class, event -> {

                initCache();
                worldLoadWait = false;

            }, new SubscriptionProperties<WorldLoadEvent>().timeOut(TimeUnit.SECONDS, 3).filter(event -> event.getWorld().getName().equals(mine.getMinPoint().getWorldName())));

            return;
        }

        Location pos1 = mine.getMinPoint().toBukkit();
        Location pos2 = mine.getHighPoint().toBukkit();

        Cuboid cuboid = new Cuboid(pos1, pos2);
        caching = true;

        cuboid.getFutureArray().whenComplete((locations, throwable) -> {
            if (throwable != null)
                throw new IllegalStateException(throwable);

            // Ensure that we're going in sync thread
            new OTask()
                    .runnable(() -> {
                        Set<OPair<Integer, Integer>> checkedChunks = new HashSet<>();

                        // Convert SPLocations to Blocks
                        Block[] newBlocks = new Block[locations.length];
                        int currentBlock = 0;

                        for (SPLocation location : locations) {
                            Location bukkitLocation = location.toBukkit();
                            int chunkX = bukkitLocation.getBlockX() >> 4;
                            int chunkZ = bukkitLocation.getBlockZ() >> 4;

                            // << CHUNK CHECK >>
                            OptionalConsumer.of(checkedChunks.stream()
                                    .filter(pair -> pair.getKey() == chunkX && pair.getSecond() == chunkZ)
                                    .findFirst())
                                    .ifNotPresent(() -> {

                                        // Ensure that the chunk is loaded
                                        if (!bukkitLocation.getWorld().isChunkLoaded(chunkX, chunkZ))
                                            bukkitLocation.getWorld().loadChunk(chunkX, chunkZ);

                                        checkedChunks.add(new OPair<>(chunkX, chunkZ));
                                    });

                            newBlocks[currentBlock] = location.toBukkit().getBlock();
                            currentBlock++;
                        }

                        this.cachedMineArea = newBlocks;
                        caching = false;
                    }).execute();
        });
    }

    @Override
    public void attach(ISuperiorMine obj) {
        this.mine = obj;
        initCache();
    }

    private <T> T[] shuffleArray(T[] array) {

        for (int i = 0; i < array.length; i++) {
            int randomPosition = ThreadLocalRandom.current().nextInt(array.length);
            T temp = array[i];
            array[i] = array[randomPosition];
            array[randomPosition] = temp;
        }

        return array;
    }

}
