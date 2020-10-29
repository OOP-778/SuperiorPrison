package com.bgsoftware.superiorprison.plugin.test.generator.manual.impl;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.bgsoftware.superiorprison.plugin.test.generator.manual.ManualObjectGenerator;
import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ManualPrestigeGenerator extends ManualObjectGenerator {
    private Map<Integer, Function<SPrisoner, ParsedObject>> registeredPrestiges;
    public ManualPrestigeGenerator(Config config) {
        super(config);
    }

    @Override
    public boolean hasNext(Object key) {
        return _getHandlerFor(getKey(key, false) + 1).isPresent();
    }

    @Override
    public boolean isValid(Object key) {
        return _getHandlerFor(getKey(key, false)).isPresent();
    }

    @Override
    public Optional<ParsedObject> getParsed(SPrisoner prisoner, Object key) {
        return _getHandlerFor(getKey(key, false))
                .map(f -> f.apply(prisoner));
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
    protected Optional<Function<SPrisoner, ParsedObject>> _getHandlerFor(Object key) {
        return Optional.ofNullable(registeredPrestiges.get(getKey(key, false)));
    }

    private Integer getKey(Object key, boolean throwIfNotFound) {
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
