package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.util.data.OQueue;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChunkDataQueue {
    private final OQueue<ChunkResetData> data = new OQueue<>();

    public ChunkResetData next() {
        if (isEmpty()) return null;
        return next(listReady());
    }

    private ChunkResetData next(OQueue<ChunkResetData> queue) {
        if (queue.isEmpty()) return null;

        ChunkResetData poll = queue.poll();
        if (!poll.isReady())
            return next(queue);

        data.remove(poll);
        return poll;
    }

    public void add(ChunkResetData chunk) {
        data.add(chunk);
    }

    public Stream<ChunkResetData> stream() {
        return data.stream();
    }

    public boolean isEmpty() {
        return stream().noneMatch(ChunkResetData::isReady);
    }

    private OQueue<ChunkResetData> listReady() {
       OQueue<ChunkResetData> queue = new OQueue<>();
       queue.addAll(stream().filter(ChunkResetData::isReady).collect(Collectors.toSet()));
       return queue;
    }
}
