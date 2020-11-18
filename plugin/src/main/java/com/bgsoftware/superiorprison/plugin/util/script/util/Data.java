package com.bgsoftware.superiorprison.plugin.util.script.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Data {
    private final Map<String, Object> placeholders = new HashMap<>();

    public void add(String key, Object value) {
        placeholders.put(key.toLowerCase(), value);
    }

    public boolean has(String key) {
        return placeholders.containsKey(key);
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(placeholders.get(key.toLowerCase()));
    }

    @Override
    public String toString() {
        return "Data{" +
                "placeholders=" + placeholders +
                '}';
    }
}
