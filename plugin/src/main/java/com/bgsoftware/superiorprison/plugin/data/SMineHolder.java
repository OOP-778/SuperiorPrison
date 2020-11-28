package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.MineHolder;
import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.ChunkDataQueue;
import com.bgsoftware.superiorprison.plugin.util.ChunkResetData;
import com.oop.datamodule.universal.UniversalStorage;
import com.oop.orangeengine.main.util.data.cache.OCache;
import com.oop.orangeengine.material.OMaterial;
import lombok.Getter;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SMineHolder extends UniversalStorage<SNormalMine> implements MineHolder {

    @Getter
    private final ChunkDataQueue queue = new ChunkDataQueue();
    private final OCache<UUID, List<SNormalMine>> minesCache = OCache
            .builder()
            .concurrencyLevel(1)
            .resetExpireAfterAccess(true)
            .expireAfter(5, TimeUnit.SECONDS)
            .build();

    private Map<String, SNormalMine> dataMap = new ConcurrentHashMap<>();

    public SMineHolder(DatabaseController controller) {
        super(controller);
        addVariant("mines", SNormalMine.class);

        currentImplementation(
                SuperiorPrisonPlugin.getInstance().getMainConfig().getStorageSection().provideFor(this, "mines")
        );
    }

    @Override
    public Set<SuperiorMine> getMines() {
        return getMines(null);
    }

    public Set<SuperiorMine> getMines(Predicate<SNormalMine> filter) {
        return stream()
                .filter(mine -> filter == null || filter.test(mine))
                .collect(Collectors.toSet());
    }

    public Set<String> getMinesWorlds() {
        return stream()
                .map(mine -> mine.getWorld().getName())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<SuperiorMine> getMine(String mineName) {
        return Optional.ofNullable(dataMap.get(mineName));
    }

    @Override
    public Optional<SuperiorMine> getMineAt(Location location) {
        return stream()
                .filter(mine -> mine.isInside(location))
                .map(mine -> (SuperiorMine) mine)
                .findFirst();
    }

    public List<SNormalMine> getMinesFor(SPrisoner prisoner) {
        List<SNormalMine> mines = minesCache.get(prisoner.getUUID());
        if (mines != null) return mines;

        mines = stream()
                .filter(mine -> prisoner.getPlayer().hasPermission("prison.admin.editmine") || (mine.canEnter(prisoner) && mine.getSettings().isTeleportation()))
                .sorted(Comparator.comparing(mine -> mine.getSettings().getOrder()))
                .collect(Collectors.toList());
        minesCache.put(prisoner.getUUID(), mines);
        return mines;
    }

    public ChunkResetData addResetBlock(Location location, OMaterial material, Runnable onComplete) {
        int chunkX, chunkZ;
        chunkX = location.getBlockX() >> 4;
        chunkZ = location.getBlockZ() >> 4;

        Optional<ChunkResetData> matchedChunk = queue
                .stream()
                .filter(chunk -> chunk.getX() == chunkX && chunk.getZ() == chunkZ)
                .findFirst();

        ChunkResetData data;

        if (matchedChunk.isPresent()) {
            data = matchedChunk.get();
            data.add(location, material, onComplete);

        } else {
            data = new ChunkResetData(location.getWorld(), chunkX, chunkZ);
            data.add(location, material, onComplete);
            queue.add(data);
        }

        return data;
    }

    public void clear() {
        minesCache.clear();
        for (SNormalMine value : dataMap.values())
            value.clean();

        dataMap.clear();
    }

    @Override
    protected void onAdd(SNormalMine sNormalMine) {
        if (sNormalMine.getSettings().getOrder() == -1)
            sNormalMine.getSettings().setOrder(dataMap.size());
        dataMap.put(sNormalMine.getName(), sNormalMine);
    }

    @Override
    protected void onRemove(SNormalMine sNormalMine) {
        dataMap.remove(sNormalMine.getName());
    }

    @Override
    public Stream<SNormalMine> stream() {
        return dataMap.values().stream();
    }

    @Override
    public Iterator<SNormalMine> iterator() {
        return dataMap.values().iterator();
    }
}
