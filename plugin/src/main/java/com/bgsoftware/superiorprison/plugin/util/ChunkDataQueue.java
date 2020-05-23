package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.util.data.OQueue;

import java.util.stream.Stream;

public class ChunkDataQueue {

    private final OQueue<ChunkResetData> data = new OQueue<>();

    public ChunkResetData next() {
        if (data.isEmpty()) return null;

        ChunkResetData poll = data.poll();
        if (!poll.isReady()) {
            data.add(poll);
            return next();
        }

        return poll;
    }

    public void add(ChunkResetData chunk) {
        data.add(chunk);
    }

    public Stream<ChunkResetData> stream() {
        return data.stream();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }
}
