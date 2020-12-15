package com.bgsoftware.superiorprison.plugin.ladder.generator.auto.generator;

import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.ladder.generator.auto.ObjectGenerator;
import com.bgsoftware.superiorprison.plugin.ladder.generator.auto.options.RankGeneratorOptions;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper;
import com.oop.orangeengine.yaml.interfaces.Valuable;

import java.math.BigInteger;

public class RankGenerator extends ObjectGenerator<RankGeneratorOptions> {
    public RankGenerator(Valuable valuable) {
        super(valuable);
    }

    @Override
    protected ParsedObject parse(SPrisoner prisoner, BigInteger index) {
        GlobalVariableMap clone = getVariableMap().clone();
        clone.newOrReplace("prisoner", VariableHelper.createVariable(prisoner));
        clone.newOrReplace("index", VariableHelper.createVariable(index));
        clone.newOrReplace("index_formatted", VariableHelper.createVariable(NumberUtil.formatBigInt(index)));
        clone.newOrReplace("rank_name", VariableHelper.createVariable(getOptions().getRankByIndex(index)));

        return ParsedObject.of(
                getOptions().getRankByIndex(index),
                getTemplate(index),
                clone,
                () -> hasNext(index) ? parse(prisoner, index.add(BigInteger.ONE)) : null,
                () -> getOptions().hasPrevious(index) ? parse(prisoner, index.subtract(BigInteger.ONE)) : null,
                index
        );
    }

    @Override
    protected void initializeMap() {
        getVariableMap().newVariable("index", VariableHelper.createVariable(1));
        getVariableMap().newVariable("rank_name", VariableHelper.createVariable("A"));
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
