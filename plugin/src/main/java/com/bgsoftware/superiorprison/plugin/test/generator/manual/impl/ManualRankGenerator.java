package com.bgsoftware.superiorprison.plugin.test.generator.manual.impl;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.bgsoftware.superiorprison.plugin.test.generator.manual.ManualObjectGenerator;
import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper;
import com.bgsoftware.superiorprison.plugin.util.DualKeyMap;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ManualRankGenerator extends ManualObjectGenerator {
    private DualKeyMap<String, Integer, Function<SPrisoner, ParsedObject>> ranksMap;

    public ManualRankGenerator(Config config) {
        super(config);
    }

    @Override
    public boolean hasNext(Object key) {
        int index = getIndex(key);
        if (index == -1) return false;

        return ranksMap.get2(index + 1) != null;
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
    public int getIndex(Object key) {
        Integer currentKey;
        if (key instanceof Number)
            currentKey = ((Number) key).intValue();
        else if (Values.isNumber(key.toString()))
            currentKey = Values.parseAsInt(key.toString());
        else {
            // Get int key by string
            currentKey = ranksMap.getKey2By1(key.toString());
        }

        return currentKey == null ? -1 : currentKey;
    }

    @Override
    public List<String> getAvailable() {
        return new ArrayList<>(ranksMap.keys().getKey());
    }

    @Override
    protected void registerObject(ConfigSection section, int index, Function<SPrisoner, ParsedObject> parser) {
        if (ranksMap == null)
            ranksMap = DualKeyMap.create(0);

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
            return Optional.ofNullable(ranksMap.get2(((Number) key).intValue()));
        else if (Values.isNumber(key.toString()))
            return Optional.ofNullable(ranksMap.get2(Values.parseAsInt(key.toString())));

        Function<SPrisoner, ParsedObject> handler = ranksMap.get1(key.toString());
        return Optional.ofNullable(handler);
    }

    @Override
    public String defaultPrefixReplacer(String in) {
        return in.replace("{rank_name}", "%rank_name%");
    }
}
