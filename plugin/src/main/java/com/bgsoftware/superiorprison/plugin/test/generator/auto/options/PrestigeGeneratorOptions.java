package com.bgsoftware.superiorprison.plugin.test.generator.auto.options;

import com.bgsoftware.superiorprison.plugin.test.generator.auto.GeneratorOptions;
import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.oop.orangeengine.yaml.ConfigSection;

public class PrestigeGeneratorOptions extends GeneratorOptions<Integer> {
    private int max;
    private int min;

    public PrestigeGeneratorOptions(ConfigSection section, GlobalVariableMap map) {
        super(map);

        section.ensureHasValues("min", "max");
        this.min = section.getAs("min");
        this.max = section.getAs("max");
    }

    @Override
    public boolean hasNext(Integer key) {
        return isValid(key + 1);
    }

    @Override
    public boolean hasPrevious(Integer key) {
        return isValid(key - 1);
    }

    @Override
    public boolean isValid(Integer key) {
        return min <= key && key <= max;
    }

    @Override
    public int getIndex(Object in) {
        if (in instanceof Number)
            return ((Number) in).intValue();

        else if (in instanceof String) {
            String s = in.toString();
            if (Values.isNumber(s))
                return Values.parseAsInt(s);
        }

        throw new IllegalStateException("Failed to find prestige by " + in);
    }
}
