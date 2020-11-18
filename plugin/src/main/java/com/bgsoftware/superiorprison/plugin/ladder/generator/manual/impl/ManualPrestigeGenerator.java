package com.bgsoftware.superiorprison.plugin.ladder.generator.manual.impl;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.ladder.generator.manual.ManualObjectGenerator;
import com.bgsoftware.superiorprison.plugin.util.script.util.Values;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ManualPrestigeGenerator extends ManualObjectGenerator {
    private Map<Integer, Function<SPrisoner, ParsedObject>> registeredPrestiges;
    public ManualPrestigeGenerator(Config config) {
        super(config);
    }

    @Override
    public boolean hasNext(Object key) {
        return this.getParser(getIndex(key, false) + 1).isPresent();
    }

    @Override
    public boolean isValid(Object key) {
        return this.getParser(getIndex(key, false)).isPresent();
    }

    @Override
    public Optional<ParsedObject> getParsed(SPrisoner prisoner, Object key) {
        return this.getParser(getIndex(key, false))
                .map(f -> f.apply(prisoner));
    }

    @Override
    public BigInteger getIndex(Object object) {
        Integer index = getIndex(object, false);
        return BigInteger.valueOf(index == null ? -1 : index);
    }

    @Override
    protected void registerObject(ConfigSection section, int index, Function<SPrisoner, ParsedObject> parser) {
        if (registeredPrestiges == null)
            registeredPrestiges = new HashMap<>();
        registeredPrestiges.put(index, parser);
    }

    @Override
    protected void handleVariableMapCreation(GlobalVariableMap map) {
    }

    @Override
    protected void handleVariableMapClone(GlobalVariableMap map, ConfigSection section) {
    }

    @Override
    public Optional<Function<SPrisoner, ParsedObject>> getParser(Object key) {
        return Optional.ofNullable(registeredPrestiges.get(getIndex(key, false)));
    }

    private Integer getIndex(Object key, boolean throwIfNotFound) {
        if (key instanceof Number)
            return ((Number) key).intValue();
        else if (Values.isNumber(key.toString()))
            return Values.parseAsInt(key.toString());

        if (throwIfNotFound)
            throw new IllegalStateException("Failed to find prestige by key `" + key + "`");

        return null;
    }

    @Override
    public String defaultPrefixReplacer(String in) {
        return in.replace("{prestige_name}", "%index%");
    }
}
