package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.util.data.pair.OPair;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ExpireableCache<T, U> {
    private final Map<T, OPair<U, Long>> data = new ConcurrentHashMap<>();

    public U get(T key) {
        this.checkForInvalids();
        OPair<U, Long> valuePair = this.data.get(key);
        return valuePair == null ? null : valuePair.getFirst();
    }

    public void clear() {
        this.data.clear();
    }

    public void remove(T key) {
        this.data.remove(key);
    }

    public OPair<U, Long> getPair(T key) {
        this.checkForInvalids();
        return this.data.get(key);
    }

    public void put(T key, U value, Long expireAfter) {
        this.checkForInvalids();
        this.data.remove(key);
        this.data.put(key, new OPair<>(value, Instant.now().getEpochSecond() + expireAfter));
    }

    private void checkForInvalids() {
        this.data.forEach((key, value) -> {
            if (value.getSecond() <= Instant.now().getEpochSecond()) {
                this.remove(key);
            }
        });
    }
}
