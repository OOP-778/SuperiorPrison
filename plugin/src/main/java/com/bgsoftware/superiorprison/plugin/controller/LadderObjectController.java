package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.ladder.ObjectSupplier;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.ladder.generator.auto.generator.PrestigeGenerator;
import com.bgsoftware.superiorprison.plugin.ladder.generator.auto.generator.RankGenerator;
import com.bgsoftware.superiorprison.plugin.ladder.generator.manual.impl.ManualPrestigeGenerator;
import com.bgsoftware.superiorprison.plugin.ladder.generator.manual.impl.ManualRankGenerator;
import com.oop.orangeengine.yaml.Config;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class LadderObjectController implements ObjectSupplier {
    private ObjectSupplier loadedSupplier;
    public LadderObjectController(Config config, boolean isPrestige) {
        // We got automatico
        if (config.isSectionPresent("template"))
            this.loadedSupplier = isPrestige ? new PrestigeGenerator(config) : new RankGenerator(config);
        else
            this.loadedSupplier = isPrestige ? new ManualPrestigeGenerator(config) : new ManualRankGenerator(config);
    }

    @Override
    public boolean hasNext(Object key) {
        return loadedSupplier.hasNext(key);
    }

    @Override
    public boolean isValid(Object key) {
        return loadedSupplier.isValid(key);
    }

    @Override
    public Optional<ParsedObject> getParsed(SPrisoner prisoner, Object key) {
        return loadedSupplier.getParsed(prisoner, key);
    }

    @Override
    public BigInteger getIndex(Object object) {
        return loadedSupplier.getIndex(object);
    }

    @Override
    public Optional<Function<SPrisoner, ParsedObject>> getParser(Object key) {
        return loadedSupplier.getParser(key);
    }

    @Override
    public BigInteger getMaxIndex() {
        return loadedSupplier.getMaxIndex();
    }
}
