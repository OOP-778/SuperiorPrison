package com.bgsoftware.superiorprison.plugin.util.placeholders.parser;

public interface PlaceholderFunction<P, T, V> {

  V get(T current, P parent, ArgsCrawler crawler);
}
