package com.bgsoftware.superiorprison.plugin.ladder.generator.auto.generator;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.ladder.generator.auto.ObjectGenerator;
import com.bgsoftware.superiorprison.plugin.ladder.generator.auto.options.PrestigeGeneratorOptions;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper;
import com.oop.orangeengine.yaml.interfaces.Valuable;

import java.math.BigInteger;
import java.util.List;

public class PrestigeGenerator extends ObjectGenerator<PrestigeGeneratorOptions> {
    public PrestigeGenerator(Valuable valuable) {
        super(valuable);
    }

    @Override
    protected ParsedObject parse(SPrisoner prisoner, BigInteger level) {
        GlobalVariableMap clone = getVariableMap().clone();
        clone.newOrReplace("prisoner", VariableHelper.createVariable(prisoner));
        clone.newOrReplace("index", VariableHelper.createVariable(level));

        return ParsedObject.of(
                level + "",
                getTemplate(level),
                clone,
                () -> getParsed(prisoner, level.add(BigInteger.ONE)).orElse(null),
                () -> getParsed(prisoner, level.subtract(BigInteger.ONE)).orElse(null),
                level
        );
    }

    @Override
    protected void initializeMap() {
        getVariableMap().newVariable("index", VariableHelper.createVariable(1));
    }

    @Override
    public BigInteger getIndex(Object object) {
        return getOptions().getIndex(object);
    }

    @Override
    public BigInteger getMaxIndex() {
        return getOptions().getMax();
    }
}
