package com.bgsoftware.superiorprison.plugin.ladder.generator.manual.impl;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.ladder.generator.manual.ManualObjectGenerator;
import com.bgsoftware.superiorprison.plugin.util.script.util.Values;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper;
import com.bgsoftware.superiorprison.plugin.util.DualKeyMap;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Function;

public class ManualRankGenerator extends ManualObjectGenerator {
    private DualKeyMap<String, Integer, Function<SPrisoner, ParsedObject>> ranksMap;

    public ManualRankGenerator(Config config) {
        super(config);
    }

    @Override
    public boolean hasNext(Object key) {
        BigInteger index = getIndex(key);

        return ranksMap.getSecond(index.intValue() + 1) != null;
    }

    @Override
    public boolean isValid(Object key) {
        return this.getParser(key).isPresent();
    }

    @Override
    public Optional<ParsedObject> getParsed(SPrisoner prisoner, Object key) {
        return this.getParser(key).map(f -> f.apply(prisoner));
    }

    @Override
    public BigInteger getIndex(Object key) {
        Integer currentKey;
        if (key instanceof Number)
            currentKey = ((Number) key).intValue();

        else if (Values.isNumber(key.toString()))
            currentKey = Values.parseAsInt(key.toString());

        else {
            // Get int key by string
            currentKey = ranksMap.getSecondKeyByFirstKey(key.toString());
        }

        return BigInteger.valueOf(currentKey == null ? -1 : currentKey);
    }

    @Override
    protected void registerObject(ConfigSection section, int index, Function<SPrisoner, ParsedObject> parser) {
        if (ranksMap == null)
            ranksMap = DualKeyMap.create(
                    0,
                    null,
                    null
            );

        String name = section.getKey();
        ranksMap.put(name, index, parser);
    }

    @Override
    protected void handleVariableMapCreation(GlobalVariableMap map) {
        map.newVariable("rank_name", VariableHelper.createVariable("Not Set"));
    }

    @Override
    protected void handleVariableMapClone(GlobalVariableMap map, ConfigSection section) {
        map.newOrReplace("rank_name", VariableHelper.createVariable(section.getKey()));
    }

    @Override
    public Optional<Function<SPrisoner, ParsedObject>> getParser(Object key) {
        if (key instanceof Number)
            return Optional.ofNullable(ranksMap.getSecond(((Number) key).intValue()));
        else if (Values.isNumber(key.toString()))
            return Optional.ofNullable(ranksMap.getSecond(Values.parseAsInt(key.toString())));

        Function<SPrisoner, ParsedObject> handler = ranksMap.getFirst(key.toString());
        return Optional.ofNullable(handler);
    }

    @Override
    public String defaultPrefixReplacer(String in) {
        return in.replace("{rank_name}", "%rank_name%");
    }
}
