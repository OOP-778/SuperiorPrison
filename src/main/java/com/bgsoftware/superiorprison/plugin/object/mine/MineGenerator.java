package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.IMineGenerator;
import com.bgsoftware.superiorprison.api.data.mine.ISuperiorMine;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.util.Cuboid;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.pair.OPair;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Setter
public class MineGenerator implements IMineGenerator, Serializable {

    private ISuperiorMine mine;
    private List<OPair<Double, Material>> generatorMaterials = new ArrayList<>();
    private transient Instant lastReset;
    private transient Instant nextReset;

    private transient Block[] cachedMineArea = new Block[]{};

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

    }
    
    public void initCache() {
        Location pos1 = mine.getMinPoint().toBukkit();
        Location pos2 = mine.getHighPoint().toBukkit();

        Cuboid cuboid = new Cuboid(pos1, pos2);
        cuboid.getFutureArray().whenComplete((locations, throwable) -> {

            // Ensure that we're going in sync thread
            new OTask()
                    .runnable(() -> {

                        // Convert SPLocations to Block
                        Block[] newBlocks = new Block[locations.length];
                        int currentBlock = 0;
                        for (SPLocation location : locations) {
                            newBlocks[currentBlock] = location.toBukkit().getBlock();
                            currentBlock++;
                        }

                        this.cachedMineArea = newBlocks;

                    });

        });
    }
}
