package com.bgsoftware.superiorprison.plugin.test.generator.auto.generator;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.bgsoftware.superiorprison.plugin.test.generator.auto.ObjectGenerator;
import com.bgsoftware.superiorprison.plugin.test.generator.auto.options.PrestigeGeneratorOptions;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper;
import com.oop.orangeengine.yaml.interfaces.Valuable;

public class PrestigeGenerator extends ObjectGenerator<PrestigeGeneratorOptions> {
    public PrestigeGenerator(Valuable valuable) {
        super(valuable);
    }

    @Override
    protected ParsedObject parse(SPrisoner prisoner, int level) {
        GlobalVariableMap clone = getVariableMap().clone();
        clone.newOrReplace("prisoner", VariableHelper.createVariable(prisoner));
        clone.newOrReplace("level", VariableHelper.createVariable(level));

        return ParsedObject.of(
                getTemplate(level),
                clone,
                () -> hasNext(level) ? getParsed(prisoner, level + 1) : null,
                () -> isValid(level - 1) ? getParsed(prisoner, level - 1) : null
        );
    }

    @Override
    protected void initializeMap() {
        getVariableMap().newVariable("level", VariableHelper.createVariable("1"));
    }
}