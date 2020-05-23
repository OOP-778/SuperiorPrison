package com.bgsoftware.superiorprison.plugin.hook.impl.parser;

public interface PlaceholderFunction<P, T, V> {

    V get(T current, P parent, ArgsCrawler crawler);

}
