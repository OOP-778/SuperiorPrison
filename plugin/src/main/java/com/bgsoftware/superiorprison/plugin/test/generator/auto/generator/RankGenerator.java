package com.bgsoftware.superiorprison.plugin.test.generator.auto.generator;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.bgsoftware.superiorprison.plugin.test.generator.auto.ObjectGenerator;
import com.bgsoftware.superiorprison.plugin.test.generator.auto.options.RankGeneratorOptions;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper;
import com.oop.orangeengine.yaml.interfaces.Valuable;

public class RankGenerator extends ObjectGenerator<RankGeneratorOptions> {
    public RankGenerator(Valuable valuable) {
        super(valuable);
    }

    @Override
    protected ParsedObject parse(SPrisoner prisoner, int level) {
        GlobalVariableMap clone = getVariableMap().clone();
        clone.newOrReplace("prisoner", VariableHelper.createVariable(prisoner));
        clone.newOrReplace("level", VariableHelper.createVariable(level));
        clone.newOrReplace("rank_name", VariableHelper.createVariable(getOptions().getRankByIndex(level)));

        return ParsedObject.of(
                getTemplate(level),
                clone,
                () -> hasNext(level) ? parse(prisoner, level + 1) : null,
                () -> isValid(level - 1) ? parse(prisoner, level - 1) : null
        );
    }

    @Override
    protected void initializeMap() {
        getVariableMap().newVariable("level", VariableHelper.createVariable(1));
        getVariableMap().newVariable("rank_name", VariableHelper.createVariable("A"));
    }
}
