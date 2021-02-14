package com.bgsoftware.superiorprison.plugin.util.reset;

import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.util.ChunkResetData;
import com.oop.orangeengine.main.util.data.OQueue;
import com.oop.orangeengine.material.OMaterial;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

@RequiredArgsConstructor
@Getter
public class ResetEntry {

    private final SNormalMine mine;
    private final Consumer<ResetEntry> whenFinished;

    private long start;
    private long end;

    private OQueue<ChunkResetData> chunkResetData = new OQueue<>();

    public void addResetBlock(Location location, OMaterial material) {
        int chunkX, chunkZ;
        chunkX = location.getBlockX() >> 4;
        chunkZ = location.getBlockZ() >> 4;

        Optional<ChunkResetData> matchedChunk =
            chunkResetData.stream()
                .filter(chunk -> chunk.getX() == chunkX && chunk.getZ() == chunkZ)
                .findFirst();

        ChunkResetData data;

        if (matchedChunk.isPresent()) {
            data = matchedChunk.get();
            data.add(location, material);

        } else {
            data = new ChunkResetData(location.getWorld().getName(), chunkX, chunkZ);
            data.add(location, material);
            chunkResetData.add(data);
        }
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public void end() {
        end = System.currentTimeMillis();
    }

    public boolean isEmpty() {
        return chunkResetData.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResetEntry that = (ResetEntry) o;
        return mine.getName().contentEquals(that.mine.getName());
    }

    @Override
    public int hashCode() {
        SArea area = mine.getArea(AreaEnum.MINE);
        return Objects.hash(mine.getName(), area.getMinPoint(), area.getHighPoint());
    }
}
