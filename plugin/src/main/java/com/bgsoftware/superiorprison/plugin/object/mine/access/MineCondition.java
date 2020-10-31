package com.bgsoftware.superiorprison.plugin.object.mine.access;

import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.script.function.Function;
import com.bgsoftware.superiorprison.plugin.test.script.util.ScriptHelper;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import lombok.Getter;

@Getter
public class MineCondition implements SerializableObject {
    private String name;
    private String plainString;
    private GlobalVariableMap variableMap = new GlobalVariableMap();
    private Function<Boolean> function;

    private MineCondition() {
        variableMap.newVariable("prisoner", VariableHelper.createNullVariable(SPrisoner.class));
        variableMap.newVariable("mine", VariableHelper.createNullVariable(SNormalMine.class));
    }

    public MineCondition(String name, String plainString) {
        this();
        this.name = name;
        this.plainString = plainString;
        initFunction();
    }

    private void initFunction() {
        try {
            function = ScriptHelper
                    .tryInitFunction(plainString, variableMap)
                    .validateTypeOrThrow(Boolean.class, Boolean.class::isAssignableFrom);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to initialize condition by `" + plainString + "`", throwable);
        }
    }

    public boolean test(GlobalVariableMap map) {
        return function.execute(map);
    }

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("name", name);
        serializedData.write("script", plainString);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        this.name = serializedData.applyAs("name", String.class);
        this.plainString = serializedData.applyAs("script", String.class);

        initFunction();
    }
}
