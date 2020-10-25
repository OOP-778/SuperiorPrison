package com.bgsoftware.superiorprison.plugin.test.generator;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;

public interface ObjectSupplier {
    boolean hasNext(Object key);

    ParsedObject getParsed(SPrisoner prisoner, Object key);
}
