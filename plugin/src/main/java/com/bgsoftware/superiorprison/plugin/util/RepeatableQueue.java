package com.bgsoftware.superiorprison.plugin.util;

import com.google.common.collect.Sets;

import java.util.Set;

public class RepeatableQueue<V> {

    private int index = 0;
    private final int size;
    private final V[] array;

    public RepeatableQueue(V[] array) {
        this.array = array;
        this.size = array.length;
    }

    public V poll() {
        if (index == (size - 1)) reset();

        V value = array[index];
        index++;
        return value;
    }

    public boolean isEmpty() {
        return index == (size - 1);
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
