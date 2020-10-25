package com.bgsoftware.superiorprison.plugin.test.generator.auto;

import com.bgsoftware.superiorprison.plugin.test.generator.auto.options.RankGeneratorOptions;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

@Getter
public abstract class GeneratorOptions<K> {
    private GlobalVariableMap variableMap;

    public GeneratorOptions(GlobalVariableMap map) {
        this.variableMap = map;
    }

    public static <G extends GeneratorOptions> G of(ConfigSection options, GlobalVariableMap variableMap) {
        if (options.isValuePresent("range"))
            return (G) new RankGeneratorOptions(options, variableMap);
        return null;
    }

    public abstract boolean hasNext(K key);

    public abstract boolean hasPrevious(K key);

    public abstract boolean isValid(K key);

    public abstract int getIndex(Object in);
}
