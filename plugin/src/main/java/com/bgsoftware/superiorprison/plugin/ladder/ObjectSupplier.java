package com.bgsoftware.superiorprison.plugin.ladder;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface ObjectSupplier {
    boolean hasNext(Object key);

    boolean isValid(Object key);

    Optional<ParsedObject> getParsed(SPrisoner prisoner, Object key);

    BigInteger getIndex(Object object);

    Optional<Function<SPrisoner, ParsedObject>> getParser(Object key);

    BigInteger getMaxIndex();
}
