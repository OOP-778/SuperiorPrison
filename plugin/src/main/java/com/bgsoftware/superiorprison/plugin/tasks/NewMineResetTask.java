package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.ChunkResetData;
import com.bgsoftware.superiorprison.plugin.util.TPS;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
import com.bgsoftware.superiorprison.plugin.util.reset.ResetEntry;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class NewMineResetTask extends OTask {
    // Used to track tps to detect drops
    private double lastTps;

    // Skip x ticks if needed
    private int skip = 0;

    // Current reset entry
    private ResetEntry entry;

    // Current chunkData
    private ChunkResetData chunkData;

    // Bukkit chunk for current chunk data
    private Chunk bukkitChunk;

    // If it's waiting for chunk load
    private boolean chunkLoad;

    public NewMineResetTask() {
        SuperiorPrisonPlugin plugin = SuperiorPrisonPlugin.getInstance();
        delay(50);
        repeat(true);
        runnable(() -> {
            if (chunkLoad) return;

            // If we have to skip, we skip!
            if (skip != 0) {
                skip--;
                return;
            }

            // Track current tps
            double tps = TPS.getCurrentTps();
            if (lastTps != 0) {
                double diff = lastTps - tps;
                if (diff > 0 && diff >= 0.25) {
                    skip = 20 * 3;
                    lastTps = 0;
                    SuperiorPrisonPlugin.getInstance()
                        .getOLogger()
                        .printDebug("Tps has gone down skipping 3 seconds");
                    return;
                }
            }

            // Check current reset entry
            if (entry == null || (entry.isEmpty() && (chunkData == null || chunkData.getData().isEmpty()))) {
                if (entry != null && chunkData != null && chunkData.getData().isEmpty()) {
                    entry.end();
                    entry.getWhenFinished().accept(entry);
                    entry = null;
                }
                ResetEntry next =
                    plugin.getMineController().getQueue().next();

                if (next == null) return;
                entry = next;
                entry.start();
            }

            // How much chunks per tick we're going thru
            int chunksPerTick = plugin.getMainConfig().getChunksPerTick();

            lastTps = plugin.getTpsController().getLastTps();
            // Go thru x chunks per cycle
            for (int currentChunk = 0; currentChunk < chunksPerTick; currentChunk++) {
                // Detection of tps drops
                double diff = lastTps - tps;
                if (diff > 0 && diff >= 0.25) {
                    skip = 20 * 3;
                    lastTps = 0;
                    SuperiorPrisonPlugin.getInstance()
                        .getOLogger()
                        .printDebug("Tps has gone down skipping 3 seconds");
                    return;
                }

                // Check for current chunk
                if (chunkData == null || chunkData.getData().isEmpty()) {
                    // We have finished resetting
                    if (entry.getChunkResetData().isEmpty())
                        return;

                    chunkData = entry.getChunkResetData().poll();

                    // Check if the chunk is loaded or not
                    World world = Bukkit.getWorld(chunkData.getWorld());
                    if (world.isChunkLoaded(chunkData.getX(), chunkData.getX())) {
                        bukkitChunk = world.getChunkAt(chunkData.getX(), chunkData.getZ());

                    } else {
                        chunkLoad = true;
                        Framework.FRAMEWORK.loadChunk(
                            world,
                            chunkData.getX(),
                            chunkData.getZ(),
                            chunk -> {
                                bukkitChunk = chunk;
                                chunkLoad = false;
                            });
                        return;
                    }
                }

                while (!chunkData.getData().isEmpty()) {
                    // Detection of tps drops
                    tps = TPS.getCurrentTps();
                    diff = lastTps - tps;
                    if (diff > 0 && diff >= 0.25) {
                        skip = 20 * 3;
                        lastTps = 0;
                        SuperiorPrisonPlugin.getInstance()
                            .getOLogger()
                            .printDebug("Tps has gone down skipping 3 seconds");
                        return;
                    }

                    OPair<Location, OMaterial> poll = chunkData.getData().poll();
                    plugin.getNms()
                        .setBlock(bukkitChunk, poll.getFirst(), poll.getSecond());
                }
            }
        });
        execute();
    }
}
