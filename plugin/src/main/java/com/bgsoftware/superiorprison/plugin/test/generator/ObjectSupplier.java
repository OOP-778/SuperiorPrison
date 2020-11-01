package com.bgsoftware.superiorprison.plugin.test.generator;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface ObjectSupplier {
    boolean hasNext(Object key);

    boolean isValid(Object key);

    Optional<ParsedObject> getParsed(SPrisoner prisoner, Object key);

    int getIndex(Object object);

    List<String> getAvailable();

    Optional<Function<SPrisoner, ParsedObject>> getParser(Object key);

    int getMaxIndex();
}
