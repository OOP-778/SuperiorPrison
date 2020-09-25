package com.bgsoftware.superiorprison.plugin.util;

import com.google.common.collect.Sets;

import java.util.Set;

public class RepeatableQueue<V> {

    private final int size;
    private final V[] array;
    private int index = 0;

    public RepeatableQueue(V[] array) {
        this.array = array;
        this.size = array.length;
    }

    public V poll() {
        if (index == size) reset();

        V value = array[index];
        index++;
        return value;
    }

    public boolean isEmpty() {
        return index == size;
    }

    public void reset() {
        index = 0;
    }

    public int size() {
        return size;
    }

    public Set<V> toSet() {
        return Sets.newHashSet(array);
    }

    public V[] array() {
        return array;
    }
}
