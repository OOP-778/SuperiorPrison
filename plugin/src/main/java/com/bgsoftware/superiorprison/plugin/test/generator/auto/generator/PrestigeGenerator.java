package com.bgsoftware.superiorprison.plugin.test.generator.auto.generator;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.bgsoftware.superiorprison.plugin.test.generator.auto.ObjectGenerator;
import com.bgsoftware.superiorprison.plugin.test.generator.auto.options.PrestigeGeneratorOptions;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper;
import com.oop.orangeengine.yaml.interfaces.Valuable;

import java.util.ArrayList;
import java.util.List;

public class PrestigeGenerator extends ObjectGenerator<PrestigeGeneratorOptions> {
    public PrestigeGenerator(Valuable valuable) {
        super(valuable);
    }

    @Override
    protected ParsedObject parse(SPrisoner prisoner, int level) {
        GlobalVariableMap clone = getVariableMap().clone();
        clone.newOrReplace("prisoner", VariableHelper.createVariable(prisoner));
        clone.newOrReplace("index", VariableHelper.createVariable(level));

        return ParsedObject.of(
                level + "",
                getTemplate(level),
                clone,
                () -> getParsed(prisoner, level + 1).orElse(null),
                () -> getParsed(prisoner, level - 1).orElse(null),
                level
        );
    }

    @Override
    protected void initializeMap() {
        getVariableMap().newVariable("index", VariableHelper.createVariable(1));
    }

    @Override
    public int getIndex(Object object) {
        return getOptions().getIndex(object);
    }

    @Override
    public List<String> getAvailable() {
        return getOptions().getAvailable();
    }

    @Override
    public int getMaxIndex() {
        return getOptions().getMax();
    }
}
