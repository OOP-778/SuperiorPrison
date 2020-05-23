package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.ChunkResetData;
import com.bgsoftware.superiorprison.plugin.util.ListenablePair;
import com.bgsoftware.superiorprison.plugin.util.TPS;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Chunk;
import org.bukkit.Location;

public class ResetQueueTask extends OTask {
    private double lastTps = 0;
    private int skip = 0;
    private final boolean cancel = false;
    private boolean running = false;

    private ChunkResetData currentChunk;
    private Chunk bukkitChunk;

    public ResetQueueTask() {
        sync(false);
        delay(100);
        repeat(true);

        runnable(() -> {
            if (skip != 0) {
                skip--;
                return;
            }

            if (currentChunk == null || currentChunk.getData().isEmpty()) {
                currentChunk = SuperiorPrisonPlugin.getInstance().getMineController().getQueue().next();
                bukkitChunk = null;
                if (currentChunk == null) return;
            }

            // Get current tps
            double tps = TPS.getCurrentTps();
            if (lastTps != 0) {
                double diff = lastTps - tps;
                if (diff > 0 && diff >= 0.3) {
                    skip = 20 * 3;
                    lastTps = 0;
                    SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Tps has gone down skipping 3 seconds");
                    return;
                }
            }

            if (lastTps == 0)
                lastTps = tps;

            if (!running) {
                running = true;
                StaticTask.getInstance().sync(() -> {
                    while (currentChunk != null && !currentChunk.getData().isEmpty()) {
                        if (cancel || currentChunk == null || currentChunk.getData().isEmpty())
                            break;

                        if (bukkitChunk == null)
                            bukkitChunk = currentChunk.getWorld().getChunkAt(currentChunk.getX(), currentChunk.getZ());

                        ListenablePair<Location, OMaterial> poll = currentChunk.getData().poll();
                        SuperiorPrisonPlugin.getInstance().getNms().setBlock(bukkitChunk, poll.getFirst(), poll.getSecond());
                        poll.complete();
                    }
                    running = false;
                });
            }
        });

        execute();
    }
}
