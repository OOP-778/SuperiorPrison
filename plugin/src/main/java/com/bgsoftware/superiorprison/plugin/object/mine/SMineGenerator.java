package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.mine.linkable.LinkableObject;
import com.bgsoftware.superiorprison.plugin.util.*;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.util.DataUtil;
import com.oop.datamodule.lib.google.gson.JsonArray;
import com.oop.datamodule.lib.google.gson.JsonElement;
import com.oop.datamodule.lib.google.gson.JsonObject;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.world.WorldLoadEvent;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.bgsoftware.superiorprison.plugin.util.TimeUtil.getDate;

@Setter
@Getter
@EqualsAndHashCode
public class SMineGenerator implements com.bgsoftware.superiorprison.api.data.mine.MineGenerator, Attachable<SuperiorMine>, SerializableObject, LinkableObject<SMineGenerator> {

    private transient SNormalMine mine;
    private List<OPair<Double, OMaterial>> generatorMaterials = new ArrayList<>();
    private transient Instant lastReset;
    private transient Instant nextReset;

    @Getter
    private transient SMineBlockData blockData = new SMineBlockData();

    @Getter
    private boolean caching;

    @Getter
    private boolean resetting;

    private transient boolean worldLoadWait;

    private transient OMaterial[] cachedMaterials;
    private transient boolean materialsChanged;

    private int blocksInRegion = -1;
    private Map<OPair<Integer, Integer>, ChunkData> cachedChunksData = new ConcurrentHashMap<>();

    @Setter
    private transient SArea mineArea;

    private AtomicLong blocksRegenerated = new AtomicLong();
    private Cuboid cuboid;

    private int lastRestartPercentage = -1;

    protected SMineGenerator() {
        caching = false;
        worldLoadWait = false;
        materialsChanged = false;
        cachedMaterials = new OMaterial[]{};
        blockData.attach(this);
    }

    public void generate() {
        if (cachedChunksData.isEmpty() || resetting || caching) return;
        if (SuperiorPrisonPlugin.getInstance() == null) return;

        resetting = true;
        Runnable executeGenerate = () -> {
            if (cachedMaterials.length == 0 || materialsChanged) {
                cachedMaterials = new OMaterial[blocksInRegion];
                RandomMaterialData data = new RandomMaterialData(generatorMaterials);

                for (int i = 0; i < blocksInRegion; i++)
                    cachedMaterials[i] = data.getMaterial();
            }

            shuffleArray(cachedMaterials);

            World world = getMine().getWorld();
            long start = System.currentTimeMillis();
            Set<ChunkResetData> data = new HashSet<>();

            Queue<Location> locationQueue = cachedChunksData.values().stream().flatMap(c -> c.getLocations().stream()).collect(Collectors.toCollection(LinkedList::new));

            Map<Chunk, Set<Location>> locations = new HashMap<>();
            cachedChunksData.values().forEach(c -> locations.put(c.chunk, c.locations));

            blockData.reset();
            for (int index = 0; index < blocksInRegion; index++) {
                Location location = locationQueue.poll();
                if (location == null) continue;

                OMaterial material = cachedMaterials[index];

                blockData.set(location, material);
                ChunkResetData chunkResetData = SuperiorPrisonPlugin.getInstance().getMineController().addResetBlock(location.clone(), material,
                        () -> {
                            long l = blocksRegenerated.incrementAndGet();
                            if (l >= blocksInRegion) {
                                ClassDebugger.debug("Finished mine resetting. Prisoners count: " + mine.getPrisoners().size());
                                SuperiorPrisonPlugin.getInstance().getNms().refreshChunks(world, locations, mine.getSpawnPoint().getWorld().getPlayers());
                                blocksRegenerated.set(0);

                                SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Finished mine {} reset. Took {}ms", mine.getName(), (System.currentTimeMillis() - start));
                                resetting = false;
                                data.clear();

                                if (!SuperiorPrisonPlugin.getInstance().getMainConfig().isUseMineDataCache()) {
                                    cachedChunksData.clear();
                                    cachedMaterials = new OMaterial[0];
                                }
                            }
                        });
                data.add(chunkResetData);
            }

            blockData.setBlocksLeft(blocksInRegion);
            data.forEach(chunkData -> chunkData.setReady(true));
        };

        if (!mine.getPendingTasks().keySet().isEmpty()) {
            new OTask()
                    .stopIf(task -> mine.getPendingTasks().keySet().isEmpty())
                    .whenFinished(executeGenerate)
                    .repeat(true)
                    .delay(200)
                    .execute();

        } else
            executeGenerate.run();
    }

    @Override
    public void reset() {
        if (resetting || caching) return;

        lastReset = getDate().toInstant();
        mine.onReset();

        // Check for cache
        StaticTask.getInstance().async(() -> {
            if (blocksInRegion == -1 || cachedChunksData.isEmpty())
                initCache(this::reset);

            else
                generate();
        });
    }

    @Override
    public Instant getWhenNextReset() {
        return ZonedDateTime.now().toInstant();
    }

    public void initCache(Runnable whenFinished) {
        if (isCaching() || isWorldLoadWait())
            return;

        if (mineArea == null)
            mineArea = mine.getArea(AreaEnum.MINE);

        if (mineArea.getWorld() == null) {
            worldLoadWait = true;
            SubscriptionFactory.getInstance().subscribeTo(WorldLoadEvent.class, event -> {

                worldLoadWait = false;
                initCache(whenFinished);

            }, new SubscriptionProperties<WorldLoadEvent>().timeOut(TimeUnit.SECONDS, 3).filter(event -> event.getWorld().getName().equals(mineArea.getMinPointSP().worldName())));
            return;
        }

        cachedChunksData.clear();

        Location pos1 = mineArea.getMinPoint();
        Location pos2 = mineArea.getHighPoint();
        cuboid = new Cuboid(pos1, pos2);
        caching = true;

        World world = pos1.getWorld();

        long start = System.currentTimeMillis();
        cuboid.getFutureArrayWithChunks().whenCompleteAsync((locations, throwable) -> {
            AtomicInteger chunkCompleted = new AtomicInteger(0);
            int required = locations.keySet().size();

            for (OPair<Integer, Integer> chunkPair : locations.keySet()) {
                Set<Location> pairLocations = locations.get(chunkPair);

                Framework.FRAMEWORK.loadChunk(world, chunkPair.getFirst(), chunkPair.getSecond(), chunk -> {
                    cachedChunksData.put(new OPair<>(chunk.getX(), chunk.getZ()), new ChunkData(chunk, pairLocations));
                    if (chunkCompleted.incrementAndGet() == required) {
                        caching = false;
                        blocksInRegion = (int) cachedChunksData.values().stream().mapToLong(c -> c.getLocations().size()).sum();

                        ClassDebugger.debug("Finished initializing cache of {} with {} blocks. Took {}ms", mine.getName(), blocksInRegion, (System.currentTimeMillis() - start));

                        if (whenFinished != null)
                            whenFinished.run();
                    }
                });
            }
        });
    }

    @Override
    public void attach(SuperiorMine obj) {
        this.mine = (SNormalMine) obj;
        this.mineArea = (SArea) obj.getArea(AreaEnum.MINE);
        this.blockData.attach(this);

        if (lastRestartPercentage == -1 || lastRestartPercentage <= SuperiorPrisonPlugin.getInstance().getMainConfig().getResetMineAtRestartAt()) {
            ClassDebugger.debug("Loading mine with reset {}", mine.getName());
            reset();

        } else {
            ClassDebugger.debug("Loading mine without reset {}, % left: {}", mine.getName(), lastRestartPercentage);
            initCache(() -> {
                resetting = true;
                StaticTask.getInstance().ensureSync(() -> {
                    try {
                        long blocksLeft = 0;
                        for (ChunkData value : cachedChunksData.values()) {
                            for (Location location : value.getLocations()) {
                                OMaterial blockType = SuperiorPrisonPlugin.getInstance().getNms().getBlockType(value.chunk, location);
                                if (blockType == null) continue;

                                blocksLeft++;
                                blockData.set(location, blockType);
                            }
                        }

                        blockData.setBlocksLeft(blocksLeft);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    resetting = false;
                });
            });
        }
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

    @Override
    public void serialize(SerializedData serializedData) {
        JsonArray array = new JsonArray();
        for (OPair<Double, OMaterial> generatorMaterial : generatorMaterials) {
            JsonObject object = new JsonObject();
            object.addProperty("m", generatorMaterial.getSecond().name());
            object.addProperty("c", generatorMaterial.getFirst().toString());
            array.add(object);
        }
        serializedData.getJsonElement().getAsJsonObject().add("materials", array);
        serializedData.write("percentage", blockData.getPercentageLeft());
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        JsonArray materialsArray = serializedData.getElement("materials").get().getAsJsonArray();
        for (JsonElement element : materialsArray) {
            JsonObject object = element.getAsJsonObject();
            generatorMaterials.add(new OPair<>(
                    DataUtil.fromElement(object.get("c"), Double.class),
                    OMaterial.valueOf(object.get("m").getAsString())
            ));
        }
        lastRestartPercentage = serializedData.applyAs("percentage", int.class, () -> -1);
    }

    @Override
    public void onChange(SMineGenerator from) {
        this.generatorMaterials.clear();
        this.generatorMaterials.addAll(from.generatorMaterials);
        setMaterialsChanged(true);
    }

    @Override
    public String getLinkId() {
        return "generator";
    }

    private class RandomMaterialData {
        private final HashSet<RandomMaterial> selection = new HashSet<>();
        private double higherBounds;

        public RandomMaterialData(List<OPair<Double, OMaterial>> list) {
            higherBounds = 0;
            list.forEach(pair -> {
                selection.add(new RandomMaterial(pair.getValue(), higherBounds, higherBounds + pair.getKey()));
                higherBounds += pair.getKey();
            });
        }

        public OMaterial getMaterial() {
            double target = ThreadLocalRandom.current().nextDouble(higherBounds);
            Optional<RandomMaterial> material = selection.stream().filter(select -> select.getLower() < target && select.getHigher() >= target).findFirst();
            return material.get().getMaterial();
        }

        @AllArgsConstructor
        @Getter
        private class RandomMaterial {
            private final OMaterial material;
            private final double lower;
            private final double higher;
        }
    }

    public void clean() {
        cachedChunksData.clear();
        blockData.getLockedBlocks().clear();
        blockData.getLocToMaterial().clear();
        blockData.getMaterials().clear();
        Arrays.fill(cachedMaterials, null);
    }

    @Getter
    @AllArgsConstructor
    private class ChunkData {
        private final Chunk chunk;
        private final Set<Location> locations;
    }
}
