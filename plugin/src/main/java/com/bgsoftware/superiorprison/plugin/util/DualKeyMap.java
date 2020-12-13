package com.bgsoftware.superiorprison.plugin.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.NonNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class DualKeyMap<K, C, V> {
    private final Map<K, V> map1;
    private final Map<C, V> map2;
    private final HashBiMap<K, C> keys = HashBiMap.create();

    private final Function<V, K> firstKeyProvider;
    private final Function<V, C> secondKeyProvider;

    private DualKeyMap(
            Map<K, V> map1,
            Map<C, V> map2,
            Function<V, K> firstKeyProvider,
            Function<V, C> secondKeyProvider
    ) {
        this.map1 = map1;
        this.map2 = map2;
        this.firstKeyProvider = firstKeyProvider;
        this.secondKeyProvider = secondKeyProvider;
    }

    public void put(K key1, C key2, V value) {
        map1.put(key1, value);
        map2.put(key2, value);

        keys.put(key1, key2);
    }

    public boolean hasByFirst(K key) {
        return map1.containsKey(key);
    }

    public K getFirstKeyBySecondKey(C key) {
        return keys.inverse().get(key);
    }

    public C getSecondKeyByFirstKey(K key) {
        return keys.get(key);
    }

    public boolean hasBySecond(C key) {
        return map2.containsKey(key);
    }

    public V getFirst(K key) {
        return map1.get(key);
    }

    public V getSecond(C key) {
        return map2.get(key);
    }

    public OPair<Set<K>, Set<C>> keys() {
        return new OPair<>(map1.keySet(), map2.keySet());
    }

    public V computeIfAbsentByFirst(
            @NonNull K key,
            @NonNull Function<K, V> valueProvider
    ) {
        V value = getFirst(key);
        if (value == null) {
            V supplied = valueProvider.apply(key);
            Preconditions.checkArgument(supplied != null, "Supplied object was null by key " + key);

            put(key, secondKeyProvider.apply(supplied), supplied);
            value = supplied;
        }

        return value;
    }

    public V computeIfAbsentBySecond(
            @NonNull C key,
            @NonNull Function<C, V> valueProvider
    ) {
        V value = getSecond(key);
        if (value == null) {
            V supplied = valueProvider.apply(key);
            Preconditions.checkArgument(supplied != null, "Supplied object was null by key " + key);

            put(firstKeyProvider.apply(supplied), key, supplied);
            value = supplied;
        }

        return value;
    }

    /**
     * @param concurrency 1 = thread safe, 0 = single threaded
     * @return DualKeyMap
     */
    public static <K, C, V> DualKeyMap<K, C, V> create(
            int concurrency,
            Function<V, K> firstKeyProvider,
            Function<V, C> secondKeyProvider
    ) {
        boolean concurrent = concurrency == 1;

        Map<K, V> map1 = concurrent ? Maps.newConcurrentMap() : Maps.newHashMap();
        Map<C, V> map2 = concurrent ? Maps.newConcurrentMap() : Maps.newHashMap();
        return new DualKeyMap<>(map1, map2, firstKeyProvider, secondKeyProvider);
    }

    public Stream<V> stream() {
        return map1.values().stream();
    }

    public Iterator<V> iterator() {
        return map1.values().iterator();
    }

    public void remove(V value) {
        map2.remove(secondKeyProvider.apply(value));
        map1.remove(firstKeyProvider.apply(value));
    }
}
