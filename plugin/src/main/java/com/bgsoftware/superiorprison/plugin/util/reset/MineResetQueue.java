package com.bgsoftware.superiorprison.plugin.util.reset;

import com.bgsoftware.superiorprison.plugin.util.ChunkResetData;
import com.oop.orangeengine.main.util.data.OQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MineResetQueue {
  private final OQueue<ResetEntry> data = new OQueue<>();

  public ResetEntry next() {
    if (isEmpty()) return null;
    return next(data);
  }

  private ResetEntry next(OQueue<ResetEntry> queue) {
    if (queue.isEmpty()) return null;
    return queue.poll();
  }

  public void add(ResetEntry chunk) {
    data.add(chunk);
  }

  public Stream<ResetEntry> stream() {
    return data.stream();
  }

  public boolean isEmpty() {
    return data.isEmpty();
  }
}
