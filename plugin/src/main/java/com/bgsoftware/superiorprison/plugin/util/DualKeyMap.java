package com.bgsoftware.superiorprison.plugin.util;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.Map;
import java.util.Set;

public class DualKeyMap<K, C, V> {
    private Map<K, V> map1;
    private Map<C, V> map2;
    private HashBiMap<K, C> keys = HashBiMap.create();

    private DualKeyMap(Map<K, V> map1, Map<C, V> map2) {
        this.map1 = map1;
        this.map2 = map2;
    }

    public void put(K key1, C key2, V value) {
        map1.put(key1, value);
        map2.put(key2, value);

        keys.put(key1, key2);
    }

    public boolean has1(K key) {
        return map1.containsKey(key);
    }

    public K getKey1By2(C key) {
        return keys.inverse().get(key);
    }

    public C getKey2By1(K key) {
        return keys.get(key);
    }

    public boolean has2(C key) {
        return map2.containsKey(key);
    }

    public V get1(K key) {
        return map1.get(key);
    }

    public V get2(C key) {
        return map2.get(key);
    }

    public OPair<Set<K>, Set<C>> keys() {
        return new OPair<>(map1.keySet(), map2.keySet());
    }

    /**
     * @param concurrency 1 = thread safe, 0 = single threaded
     * @return DualKeyMap
     */
    public static <K, C, V> DualKeyMap<K, C, V> create(
            int concurrency
    ) {
        boolean concurrent = concurrency == 1;

        Map<K, V> map1 = concurrent ? Maps.newConcurrentMap() : Maps.newHashMap();
        Map<C, V> map2 = concurrent ? Maps.newConcurrentMap() : Maps.newHashMap();
        return new DualKeyMap<>(map1, map2);
    }
}
