package com.bgsoftware.superiorprison.plugin.ladder.generator.auto.options;

import com.bgsoftware.superiorprison.plugin.ladder.generator.auto.GeneratorOptions;
import com.bgsoftware.superiorprison.plugin.util.script.util.Values;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public class PrestigeGeneratorOptions extends GeneratorOptions<BigInteger> {
    private final BigInteger max;
    private final BigInteger min;

    public PrestigeGeneratorOptions(ConfigSection section, GlobalVariableMap map) {
        super(map);

        section.ensureHasValues("min", "max");
        this.min = new BigInteger(section.getAs("min", String.class));
        this.max = new BigInteger(section.getAs("max", String.class));
    }

    @Override
    public boolean hasNext(BigInteger key) {
        return isValid(key.add(BigInteger.ONE));
    }

    @Override
    public boolean hasPrevious(BigInteger key) {
        return isValid(key.subtract(BigInteger.ONE));
    }

    @Override
    public boolean isValid(BigInteger key) {
        int minCompare = min.compareTo(key);
        int maxCompare = max.compareTo(key);
        return minCompare <= 0 && maxCompare >= 0;
    }

    @Override
    public BigInteger getIndex(Object in) {
        if (in instanceof BigInteger)
            return (BigInteger) in;

        if (in instanceof Number)
            return BigInteger.valueOf(((Number) in).longValue());

        else if (in instanceof String) {
            String s = in.toString();
            if (Values.isNumber(s))
                return Values.parseAsBigInt(s);
        }

        throw new IllegalStateException("Failed to find prestige by " + in);
    }
}
