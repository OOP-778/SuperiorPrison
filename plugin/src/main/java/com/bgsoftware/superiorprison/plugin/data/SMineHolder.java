package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.MineHolder;
import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.ChunkDataQueue;
import com.bgsoftware.superiorprison.plugin.util.ChunkResetData;
import com.google.common.collect.Maps;
import com.oop.datamodule.storage.SqlStorage;
import com.oop.orangeengine.main.util.data.cache.OCache;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.main.util.data.pair.OTriplePair;
import com.oop.orangeengine.material.OMaterial;
import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SMineHolder extends SqlStorage<SNormalMine> implements MineHolder {
    private final Map<String, SNormalMine> mineMap = Maps.newConcurrentMap();

    @Getter
    private final ChunkDataQueue queue = new ChunkDataQueue();

    public SMineHolder(DatabaseController controller) {
        super(controller, controller.getDatabase());
    }

    @Override
    public Stream<SNormalMine> stream() {
        return mineMap.values().stream();
    }

    @Override
    public Class<? extends SNormalMine>[] getVariants() {
        return new Class[]{SNormalMine.class};
    }

    @Override
    public void onAdd(SNormalMine sNormalMine) {
        mineMap.put(sNormalMine.getName(), sNormalMine);
    }

    @Override
    public void onRemove(SNormalMine sNormalMine) {
        mineMap.remove(sNormalMine.getName());
    }

    @Override
    public Set<SuperiorMine> getMines() {
        return getMines(null);
    }

    public Set<SuperiorMine> getMines(Predicate<SuperiorMine> filter) {
        return stream()
                .map(mine -> (SuperiorMine) mine)
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
        return stream()
                .filter(mine -> mine.getName().contentEquals(mineName))
                .map(mine -> (SuperiorMine) mine)
                .findFirst();
    }

    @Override
    public Optional<SuperiorMine> getMineAt(Location location) {
        return stream()
                .filter(mine -> mine.isInside(location))
                .map(mine -> (SuperiorMine) mine)
                .findFirst();
    }

    private OCache<UUID, List<SNormalMine>> minesCache = OCache
            .builder()
            .concurrencyLevel(1)
            .resetExpireAfterAccess(true)
            .expireAfter(5, TimeUnit.SECONDS)
            .build();

    public List<SNormalMine> getMinesFor(SPrisoner prisoner) {
        List<SNormalMine> mines = minesCache.get(prisoner.getUUID());
        if (mines != null) return mines;

        mines = stream()
                .filter(mine -> prisoner.getPlayer().hasPermission("prison.admin.editmine") || (mine.canEnter(prisoner) && mine.getSettings().isTeleporation()))
                .sorted(Comparator.comparing(SNormalMine::getName))
                .collect(Collectors.toList());
        minesCache.put(prisoner.getUUID(), mines);
        return mines;
    }

    @Override
    public Iterator<SNormalMine> iterator() {
        return mineMap.values().iterator();
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
}
