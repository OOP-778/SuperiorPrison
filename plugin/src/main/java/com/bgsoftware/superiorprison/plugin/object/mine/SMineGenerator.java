package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.util.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.util.DataUtil;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.bgsoftware.superiorprison.plugin.util.TimeUtil.getDate;
import static com.oop.orangeengine.main.Engine.getEngine;

@Setter
@Getter
@EqualsAndHashCode
public class SMineGenerator implements com.bgsoftware.superiorprison.api.data.mine.MineGenerator, Attachable<SuperiorMine>, SerializableObject {

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
    private Map<OPair<Integer, Integer>, Chunk> cachedChunks = new ConcurrentHashMap<>();
    private RepeatableQueue<Location> locationsQueue;

    private Map<Chunk, Set<Location>> cachedLocations = new HashMap<>();

    @Setter
    private transient SArea mineArea;

    private AtomicLong blocksRegenerated = new AtomicLong();

    protected SMineGenerator() {
        caching = false;
        worldLoadWait = false;
        materialsChanged = false;
        cachedMaterials = new OMaterial[]{};
        blockData.attach(this);
    }

    public void generate() {
        if (cachedChunks.isEmpty() || resetting || caching || locationsQueue == null) return;

        resetting = true;
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

            blockData.initialize();
        }

        shuffleArray(cachedMaterials);

        World world = getMine().getWorld();
        ZonedDateTime dateTime = getDate();
        Set<ChunkResetData> data = new HashSet<>();

        for (int index = 0; index < blocksInRegion; index++) {
            Location location = locationsQueue.poll();
            OMaterial material = cachedMaterials[index];
            if (material == null) continue;

            ChunkResetData chunkResetData = SuperiorPrisonPlugin.getInstance().getMineController().addResetBlock(location, material,
                    () -> {
                        long l = blocksRegenerated.incrementAndGet();
                        //System.out.println("Completed " + l + "/" + blocksInRegion);
                        if (l >= blocksInRegion) {
                            ClassDebugger.debug("Finished mine resetting. Prisoners count: " + mine.getPrisoners().size());
                            SuperiorPrisonPlugin.getInstance().getNms().refreshChunks(world, cachedLocations, mine.getPrisoners().stream().filter(Prisoner::isOnline).map(Prisoner::getPlayer).collect(Collectors.toSet()));
                            blocksRegenerated.set(0);
                            blockData.reset();

                            SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Finished reseting mine. Took " + (Duration.between(dateTime, getDate()).getSeconds() + "s"));
                            locationsQueue.reset();
                            resetting = false;
                            data.clear();
                        }
                    });
            data.add(chunkResetData);
        }

        data.forEach(chunkData -> chunkData.setReady(true));
    }

    @Override
    public void reset() {
        // Check for cache
        StaticTask.getInstance().async(() -> {
            if (blocksInRegion == -1)
                initCache(this::reset);

            else
                generate();
        });
        lastReset = getDate().toInstant();
        mine.onReset();
    }

    @Override
    public Instant getWhenNextReset() {
        return ZonedDateTime.now().toInstant();
    }

    public void initCache(Runnable whenFinished) {
        if (isCaching() || isWorldLoadWait())
            return;

        if (mineArea == null)
            mineArea = (SArea) mine.getArea(AreaEnum.MINE);

        if (mineArea.getWorld() == null) {
            worldLoadWait = true;
            SubscriptionFactory.getInstance().subscribeTo(WorldLoadEvent.class, event -> {

                worldLoadWait = false;
                initCache(whenFinished);

            }, new SubscriptionProperties<WorldLoadEvent>().timeOut(TimeUnit.SECONDS, 3).filter(event -> event.getWorld().getName().equals(mineArea.getMinPointSP().worldName())));
            return;
        }

        cachedChunks.clear();
        Location pos1 = mineArea.getMinPoint();
        Location pos2 = mineArea.getHighPoint();

        Cuboid cuboid = new Cuboid(pos1, pos2);
        caching = true;

        World world = pos1.getWorld();
        cuboid.getFutureArrayWithChunks().whenCompleteAsync((locations, throwable) -> {
            try {
                Set<Chunk> chunks = StaticTask.getInstance().gatherFromSync(() -> {
                    Set<Chunk> toReturn = new HashSet<>();
                    for (OPair<Integer, Integer> chunkPair : locations.keySet()) {
                        toReturn.add(world.getChunkAt(chunkPair.getFirst(), chunkPair.getSecond()));
                    }
                    return toReturn;
                }).get();

                List<Location> tempLocations = new ArrayList<>();
                for (Chunk chunk : chunks) {
                    Set<Location> chunkLocations = locations.get(new OPair<>(chunk.getX(), chunk.getZ()))
                            .stream()
                            .map(SPLocation::toBukkit)
                            .collect(Collectors.toSet());
                    tempLocations.addAll(chunkLocations);
                    cachedChunks.put(new OPair<>(chunk.getX(), chunk.getZ()), chunk);
                }

                locationsQueue = new RepeatableQueue<>(tempLocations.toArray(new Location[0]));
                blocksInRegion = locationsQueue.size();

                ClassDebugger.debug("Initialized cache");
                ClassDebugger.debug("Blocks: " + blocksInRegion);

                for (Location location1 : locationsQueue.array()) {
                    Set<Location> locations1 = cachedLocations.computeIfAbsent(cachedChunks.get(new OPair<>(location1.getBlockX() >> 4, location1.getBlockZ() >> 4)), pair -> new HashSet<>());
                    locations1.add(location1);
                }
                caching = false;

                if (whenFinished != null)
                    whenFinished.run();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void attach(SuperiorMine obj) {
        this.mine = (SNormalMine) obj;
        this.mineArea = (SArea) obj.getArea(AreaEnum.MINE);
        this.blockData.attach(this);
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

    @Override
    public void serialize(SerializedData serializedData) {
        JsonArray array = new JsonArray();
        for (OPair<Double, OMaterial> generatorMaterial : generatorMaterials) {
            JsonObject object = new JsonObject();
            object.addProperty("m", generatorMaterial.getSecond().name());
            object.addProperty("c", generatorMaterial.getFirst().toString());
            array.add(object);
        }
        serializedData.getJsonObject().add("materials", array);
        serializedData.write("blockData", blockData);
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
        blockData = serializedData.applyAs("blockData", SMineBlockData.class, () -> new SMineBlockData());
        blockData.attach(this);
    }
}
