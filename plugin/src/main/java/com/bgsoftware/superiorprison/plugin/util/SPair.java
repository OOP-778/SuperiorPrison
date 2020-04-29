package com.bgsoftware.superiorprison.plugin.util;

import com.bgsoftware.superiorprison.api.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class SPair<K, V> implements Pair<K, V> {
    private K key;
    private V value;

    public <T> T getKey(Class<T> clazz) {
        return (T) key;
    }

    public <T> T getValue(Class<T> clazz) {
        return (T) value;
    }
}
