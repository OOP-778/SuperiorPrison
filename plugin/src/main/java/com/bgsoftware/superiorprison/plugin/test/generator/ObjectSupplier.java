package com.bgsoftware.superiorprison.plugin.test.generator;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;

import java.util.Optional;

public interface ObjectSupplier {
    boolean hasNext(Object key);

    boolean isValid(Object key);

    Optional<ParsedObject> getParsed(SPrisoner prisoner, Object key);
}
