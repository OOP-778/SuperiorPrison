package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.ChunkResetData;
import com.bgsoftware.superiorprison.plugin.util.ListenablePair;
import com.bgsoftware.superiorprison.plugin.util.TPS;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.main.util.version.OVersion;
import com.oop.orangeengine.material.OMaterial;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;

public class ResetQueueTask extends OTask {
    private double lastTps = 0;
    private int skip = 0;
    private boolean cancel = false;
    private boolean gettingChunk = false;
    private boolean running = false;

    @Setter
    private int chunksPerTick = 4;

    private volatile ChunkResetData currentChunk;

    // Bukkit Chunk, if force loaded
    private OPair<Chunk, Boolean> bukkitChunk;

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
                if (currentChunk == null)
                    return;
            }

            // Get current tps
            double tps = TPS.getCurrentTps();
            if (lastTps != 0) {
                double diff = lastTps - tps;
                if (diff > 0 && diff >= 0.25) {
                    skip = 20 * 3;
                    lastTps = 0;
                    cancel = true;
                    SuperiorPrisonPlugin.getInstance().getOLogger().printDebug("Tps has gone down skipping 3 seconds");
                    return;
                }
            }

            if (lastTps == 0)
                lastTps = tps;

            if (!running && !gettingChunk) {
                running = true;
                cancel = false;

                StaticTask.getInstance().sync(() -> {
                    int proceedChunks = 0;
                    try {
                        while (currentChunk != null && !currentChunk.getData().isEmpty()) {
                            if (cancel || currentChunk == null || currentChunk.getData().isEmpty())
                                break;

                            if (bukkitChunk == null) {
                                gettingChunk = true;
                                Framework.FRAMEWORK.loadChunk(currentChunk.getWorld(), currentChunk.getX(), currentChunk.getZ(), chunk -> {
                                    if (OVersion.isAfter(16)) {
                                        chunk.addPluginChunkTicket(SuperiorPrisonPlugin.getInstance());
                                        this.bukkitChunk = new OPair<>(chunk, true);
                                    } else
                                        this.bukkitChunk = new OPair<>(chunk, false);

                                    this.gettingChunk = false;
                                });
                                break;
                            }

                            ListenablePair<Location, OMaterial> poll = currentChunk.getData().poll();
                            SuperiorPrisonPlugin.getInstance().getNms().setBlock(bukkitChunk.getFirst(), poll.getFirst(), poll.getSecond());
                            poll.complete();

                            if (currentChunk != null && currentChunk.getData().isEmpty()) {
                                proceedChunks++;
                                currentChunk = SuperiorPrisonPlugin.getInstance().getMineController().getQueue().next();

                                if (bukkitChunk.getValue())
                                    bukkitChunk.getKey().removePluginChunkTicket(SuperiorPrisonPlugin.getInstance());

                                bukkitChunk = null;
                                if (proceedChunks == chunksPerTick)
                                    break;
                            }
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    running = false;
                });
            }
        });
    }
}
