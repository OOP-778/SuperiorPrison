package com.bgsoftware.superiorprison.plugin.util;

import java.util.List;
import java.util.function.Consumer;

public interface LoadHookable<T> {
    List<Consumer<T>> getLoadHooks();

    default void addLoadHook(Consumer<T> consumer) {
        getLoadHooks().add(consumer);
    }
}
