package com.bgsoftware.superiorprison.plugin.util.placeholders.parser;

import java.util.HashMap;
import java.util.Map;

public class ObjectCache {

    public Map<Class, Object> objects = new HashMap<>();

    public <T> T getAs(Class<T> clazz) {
        return (T) objects.get(clazz);
    }

    public ObjectCache add(Object obj) {
        objects.put(obj.getClass(), obj);
        return this;
    }
}
