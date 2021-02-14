package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.ChunkResetData;
import com.bgsoftware.superiorprison.plugin.util.TPS;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
import com.bgsoftware.superiorprison.plugin.util.reset.ResetEntry;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class ResetQueueTask extends OTask {
  private double lastTps = 0;
  private int skip = 0;

  @Setter private int chunksPerTick = 4;

  private volatile QueueState state = QueueState.WAITING;

  private volatile ResetEntry currentEntry;
  private volatile ChunkResetData currentChunk;

  // Bukkit Chunk, if force loaded
  private OPair<Chunk, Boolean> bukkitChunk;

  public ResetQueueTask() {
    sync(false);
    delay(50);
    repeat(true);

    runnable(
        () -> {
          if (SuperiorPrisonPlugin.getInstance().isDisabling()) return;
          if (skip != 0) {
            skip--;
            return;
          }

          // Handle getting new reset entry
          if (currentEntry == null || currentEntry.isEmpty()) {
              if (currentEntry != null && currentEntry.isEmpty())
                  currentEntry.getWhenFinished().accept(currentEntry);
            ResetEntry next =
                SuperiorPrisonPlugin.getInstance().getMineController().getQueue().next();

            if (next == null) return;
            currentEntry = next;
          }

          // Handle getting new reset entry chunk
          if (state != QueueState.RUNNING
              && (currentChunk == null || currentChunk.getData().isEmpty())) {
            if (currentEntry.isEmpty()) return;
            currentChunk = currentEntry.getChunkResetData().poll();
            bukkitChunk = null;
            if (currentChunk == null) return;
          }

          // Get current tps
          double tps = TPS.getCurrentTps();
          if (lastTps != 0) {
            double diff = lastTps - tps;
            if (diff > 0 && diff >= 0.25) {
              skip = 20;
              lastTps = 0;
              state = QueueState.TPS_DROP;
              SuperiorPrisonPlugin.getInstance()
                  .getOLogger()
                  .printDebug("Tps has gone down skipping 3 seconds");
              return;
            }
          }

          if (lastTps == 0) lastTps = tps;

          if (state != QueueState.RUNNING && state != QueueState.LOADING_CHUNK) {
            new OTask()
                .consumer(
                    (inTask) -> {
                      if (state == QueueState.CANCEL) {
                        inTask.cancel();
                        state = QueueState.WAITING;
                      }

                      if (state == QueueState.LOADING_CHUNK) return;
                      // If current chunk data is empty, return
                      if (currentChunk.getData().isEmpty()) {
                        inTask.cancel();
                        currentChunk = null;
                        state = QueueState.EMPTY;

                        bukkitChunk = null;
                        return;
                      }

                      // Load chunk if not present
                      if (bukkitChunk == null) {
                        World world = Bukkit.getWorld(currentChunk.getWorld());
                        if (world.isChunkLoaded(currentChunk.getX(), currentChunk.getX())) {
                          bukkitChunk =
                              new OPair<>(
                                  world.getChunkAt(currentChunk.getX(), currentChunk.getZ()),
                                  false);
                          state = QueueState.RUNNING;
                        } else {
                          Framework.FRAMEWORK.loadChunk(
                              world,
                              currentChunk.getX(),
                              currentChunk.getZ(),
                              chunk -> {
                                bukkitChunk = new OPair<>(chunk, true);
                                state = QueueState.RUNNING;
                              });
                          state = QueueState.LOADING_CHUNK;
                          return;
                        }
                      }

                      if (state == QueueState.RUNNING) {
                        OPair<Location, OMaterial> poll = currentChunk.getData().poll();
                        if (poll == null) return;

                        SuperiorPrisonPlugin.getInstance()
                            .getNms()
                            .setBlock(bukkitChunk.getFirst(), poll.getFirst(), poll.getSecond());
                      }
                    })
                .sync(true)
                .delay(50)
                .repeat(true)
                .execute();
          }
        });
  }

  private enum QueueState {
    LOADING_CHUNK,
    WAITING,
    RUNNING,
    TPS_DROP,
    CANCEL,
    EMPTY
  }
}
