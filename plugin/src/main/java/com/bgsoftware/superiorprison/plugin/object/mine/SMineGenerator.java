package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.bgsoftware.superiorprison.plugin.util.Cuboid;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.main.gson.GsonUpdateable;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.world.WorldLoadEvent;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.oop.orangeengine.main.Helper.debug;

@Setter
@Getter
@EqualsAndHashCode
public class SMineGenerator implements com.bgsoftware.superiorprison.api.data.mine.MineGenerator, GsonUpdateable, Attachable<SuperiorMine> {

    private transient SuperiorMine mine;

    @SerializedName(value = "generatorMaterials")
    private List<OPair<Double, OMaterial>> generatorMaterials = new ArrayList<>();

    @SerializedName(value = "nonEmptyBlocks")
    private int nonEmptyBlocks = 0;

    private transient Instant lastReset;
    private transient Instant nextReset;
    private transient BlockChanger blockChanger;

    // << CACHING >>
    private transient Block[] cachedMineArea;
    private transient boolean caching;
    private transient boolean worldLoadWait;

    private transient OMaterial[] cachedMaterials;
    private transient boolean materialsChanged;

    private transient List<Chunk> cachedChunks;

    protected SMineGenerator() {
        caching = false;
        cachedMineArea = new Block[]{};
        worldLoadWait = false;
        materialsChanged = false;
        cachedChunks = new ArrayList<>();
        cachedMaterials = new OMaterial[]{};
    }

    public void generate() {
        initBlockChanger();
        if (cachedMineArea.length == 0) return;

        int blocksInRegion = cachedMineArea.length;
        debug("blocks in region: " + blocksInRegion);

        if (cachedMaterials.length == 0 || materialsChanged) {

            cachedMaterials = new OMaterial[blocksInRegion];
            int slot = 0;
            for (OPair<Double, OMaterial> generatorMaterial : generatorMaterials) {
                int amount = (int) Math.round((generatorMaterial.getFirst() / 100d) * blocksInRegion) + 1;
                for (int i = 0; i < amount; i++) {
                    if (Math.abs(blocksInRegion - slot) <= 0)
                        break;

                    cachedMaterials[slot] = generatorMaterial.getSecond();
                    slot++;
                }
            }
        }
        shuffleArray(cachedMaterials);
        nonEmptyBlocks = cachedMaterials.length;

        debug("materials amount: " + Arrays.stream(cachedMaterials).filter(Objects::nonNull).toArray().length);
        for (int index = 0; index < blocksInRegion; index++) {
            Block block = cachedMineArea[index];
            OMaterial material = cachedMaterials[index];
            if (material == null) continue;

            blockChanger.setBlock(block.getLocation(), material);
        }
        blockChanger.submitUpdate();
    }

    @Override
    public void reset() {
        // Check for cache
        StaticTask.getInstance().async(() -> {
            if (cachedMineArea.length == 0)
                initCache(this::reset);

            else
                generate();
        });
        lastReset = ZonedDateTime.now().toInstant();
    }

    @Override
    public int getPercentageOfFullBlocks() {
        if (cachedMineArea.length == 0) return 0;
        return (int) Math.round(nonEmptyBlocks * 100.0 / cachedMineArea.length);
    }

    @Override
    public Instant getWhenNextReset() {
        return ZonedDateTime.now().toInstant();
    }

    public void generateAir() {
        initBlockChanger();

        for (Block block : cachedMineArea) {
            if (block == null) continue;
            blockChanger.setBlock(block.getLocation(), OMaterial.AIR);
        }
        blockChanger.submitUpdate();

        debug("Cached chunks: " + cachedChunks.size());
        SuperiorPrisonPlugin.getInstance().getNms().refreshChunks(mine.getMinPoint().getWorld(), cachedChunks);
    }

    public void initCache(Runnable whenFinished) {
        if (isCaching() || isWorldLoadWait() || cachedMineArea.length > 0)
            return;

        if (mine.getMinPoint().getWorld() == null) {
            worldLoadWait = true;
            SubscriptionFactory.getInstance().subscribeTo(WorldLoadEvent.class, event -> {

                worldLoadWait = false;
                initCache(whenFinished);

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

    public double getCurrentUsedRate() {
        double[] rate = new double[]{0};
        generatorMaterials.forEach(pair -> rate[0] = rate[0] + pair.getFirst());

        return rate[0];
    }

    public double getCurrentUsedRate(OMaterial minus) {
        double[] rate = new double[]{0};
        generatorMaterials.forEach(pair -> {
            if (pair.getSecond() == minus) return;
            rate[0] = rate[0] + pair.getFirst();
        });

        return rate[0];
    }

    public void initBlockChanger() {
        if (blockChanger == null)
            blockChanger = new BlockChanger(this, getMine().getHighPoint().getWorld());
    }

    private class BlockChanger {

        private final Set<ChunkPosition> chunkPositions = Sets.newConcurrentHashSet();
        private final SuperiorPrisonPlugin plugin;

        private World world;
        private SMineGenerator generator;

        ExecutorService executor;

        public BlockChanger(@NonNull SMineGenerator generator, @NonNull World world) {
            this.plugin = SuperiorPrisonPlugin.getInstance();
            this.world = world;
            this.generator = generator;
        }

        public void setBlock(@NonNull Location location, @NonNull OMaterial material) {
            if (executor == null || executor.isTerminated() || executor.isShutdown())
                executor = Executors.newCachedThreadPool();

            ChunkPosition chunkPosition = new ChunkPosition(location.getBlockX() >> 4, location.getBlockZ() >> 4);
            chunkPositions.add(chunkPosition);
            executor.execute(() -> plugin.getNms().setBlock(location, material));
        }

        public void submitUpdate() {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    executor.shutdown();
                    executor.awaitTermination(1, TimeUnit.MINUTES);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getNms().refreshChunks(world, chunkPositions.stream().map(chunkPos -> generator.mine.getHighPoint().getWorld().getChunkAt(chunkPos.x, chunkPos.z)).filter(Objects::nonNull).collect(Collectors.toList()));
                    chunkPositions.clear();
                });
            });
        }

        private class ChunkPosition {

            private int x, z;

            ChunkPosition(int x, int z) {
                this.x = x;
                this.z = z;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ChunkPosition that = (ChunkPosition) o;
                return x == that.x &&
                        z == that.z;
            }

            @Override
            public int hashCode() {
                return Objects.hash(x, z);
            }
        }
    }

}
