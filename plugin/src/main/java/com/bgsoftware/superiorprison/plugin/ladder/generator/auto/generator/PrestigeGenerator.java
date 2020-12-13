package com.bgsoftware.superiorprison.plugin.ladder.generator.auto.generator;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.ladder.generator.auto.ObjectGenerator;
import com.bgsoftware.superiorprison.plugin.ladder.generator.auto.options.PrestigeGeneratorOptions;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
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
    protected ParsedObject parse(SPrisoner prisoner, BigInteger index) {
        GlobalVariableMap clone = getVariableMap().clone();
        clone.newOrReplace("prisoner", VariableHelper.createVariable(prisoner));
        clone.newOrReplace("index", VariableHelper.createVariable(index));
        clone.newOrReplace("index_formatted", VariableHelper.createVariable(NumberUtil.formatBigInt(index)));

        return ParsedObject.of(
                index + "",
                getTemplate(index),
                clone,
                () -> getParsed(prisoner, index.add(BigInteger.ONE)).orElse(null),
                () -> getParsed(prisoner, index.subtract(BigInteger.ONE)).orElse(null),
                index
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
