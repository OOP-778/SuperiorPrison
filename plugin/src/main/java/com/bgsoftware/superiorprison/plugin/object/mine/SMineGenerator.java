package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.bgsoftware.superiorprison.plugin.util.Cuboid;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.world.WorldLoadEvent;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin.debug;

@Setter
@Getter
public class SMineGenerator implements com.bgsoftware.superiorprison.api.data.mine.MineGenerator, Serializable, Attachable<SuperiorMine> {

    private transient SuperiorMine mine;
    private List<OPair<Double, OMaterial>> generatorMaterials = new ArrayList<>();
    private transient Instant lastReset;
    private transient Instant nextReset;

    // << CACHING >>
    private transient Block[] cachedMineArea = new Block[]{};
    private transient boolean caching = false;
    private transient boolean worldLoadWait = false;

    private OMaterial[] cachedMaterials = new OMaterial[]{};
    private transient boolean materialsChanged = false;

    private transient List<Chunk> cachedChunks = new ArrayList<>();

    protected SMineGenerator() {}

    @Override
    public Instant getLastReset() {
        return lastReset;
    }

    public void generate() {
        if (cachedMineArea.length == 0) return;

        int blocksInRegion = cachedMineArea.length;
        if (debug)
            System.out.println("blocks in region: " + blocksInRegion);

        if (cachedMaterials.length == 0 || materialsChanged) {

            cachedMaterials = new OMaterial[blocksInRegion];
            int slot = 0;
            for (OPair<Double, OMaterial> generatorMaterial : generatorMaterials) {
                int amount = (int) Math.round((generatorMaterial.getFirst() / 100d) * blocksInRegion);
                for (int i = 0; i < amount; i++) {
                    if (Math.abs(blocksInRegion - slot) <= 1)
                        break;

                    cachedMaterials[slot] = generatorMaterial.getSecond();
                    slot++;
                }
            }
        }

        if (debug) {
            System.out.println("materials amount: " + Arrays.stream(cachedMaterials).filter(Objects::nonNull).toArray().length);
        }

        cachedMaterials = shuffleArray(cachedMaterials);
        for (int index = 0; index < blocksInRegion; index++) {
            Block block = cachedMineArea[index];
            OMaterial material = cachedMaterials[index];
            if (material == null) continue;

            SuperiorPrisonPlugin.getInstance().getNmsHandler().setBlock(block.getLocation(), material);
        }
        SuperiorPrisonPlugin.getInstance().getNmsHandler().refreshChunks(mine.getMinPoint().getWorld(), cachedChunks);
    }

    public void clearMine() {
        for (Block block : cachedMineArea) {
            if (block == null) continue;

            SuperiorPrisonPlugin.getInstance().getNmsHandler().setBlock(block.getLocation(), OMaterial.AIR);
        }
        System.out.println("Cached chunks: " + cachedChunks.size());
        SuperiorPrisonPlugin.getInstance().getNmsHandler().refreshChunks(mine.getMinPoint().getWorld(), cachedChunks);
    }

    public void initCache(Runnable whenFinished) {
        if (isCaching() || isWorldLoadWait())
            return;

        if (mine.getMinPoint().getWorld() == null) {
            worldLoadWait = true;
            SubscriptionFactory.getInstance().subscribeTo(WorldLoadEvent.class, event -> {

                initCache(whenFinished);
                worldLoadWait = false;

            }, new SubscriptionProperties<WorldLoadEvent>().timeOut(TimeUnit.SECONDS, 3).filter(event -> event.getWorld().getName().equals(mine.getMinPoint().worldName())));

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
                        cachedChunks.clear();
                        Set<OPair<Integer, Integer>> checkedChunks = new HashSet<>();

                        // Convert SPLocations to Blocks
                        Block[] newBlocks = new Block[locations.length];
                        int currentBlock = 0;

                        for (SPLocation location : locations) {
                            if (location == null) continue;

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
                                        cachedChunks.add(bukkitLocation.getWorld().getChunkAt(chunkX, chunkZ));
                                    });

                            newBlocks[currentBlock] = location.toBukkit().getBlock();
                            currentBlock++;
                        }

                        cachedMineArea = Arrays.stream(newBlocks).filter(Objects::nonNull).toArray(Block[]::new);
                        caching = false;

                        if (whenFinished != null)
                            whenFinished.run();
                    }).execute();
        });
    }

    @Override
    public void attach(SuperiorMine obj) {
        this.mine = obj;
        cachedChunks = new ArrayList<>();
        cachedMineArea = new Block[]{};
        initCache(null);
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
