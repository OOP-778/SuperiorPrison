package com.bgsoftware.superiorprison.plugin.test.requirement;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.script.function.Function;
import com.bgsoftware.superiorprison.plugin.test.script.util.Data;
import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.ConfigValue;
import lombok.Getter;
import org.bukkit.Bukkit;

import static com.bgsoftware.superiorprison.plugin.test.script.util.ScriptHelper.getFunctionFromObject;
import static com.bgsoftware.superiorprison.plugin.test.script.util.ScriptHelper.tryInitFunction;

@Getter
public class RequirementData {
    private Function<Object> getter;
    private Function<Boolean> taker;
    private Function<Object> checkValue;
    private Function<Boolean> check;
    private String displayName;

    public RequirementData(ConfigSection section, GlobalVariableMap varMap) {
        section.ensureHasValues("getter", "value", "checker");

        Object getter = section.get("getter").get().getObject();
        if (getter instanceof Number || Values.isNumber(getter.toString()))
            this.getter = getFunctionFromObject(Values.parseAsInt(getter.toString()));
        else
            this.getter = tryInitFunction(getter.toString(), varMap)
                    .get();

        Object value = section.get("value").get().getObject();
        System.out.println(value.getClass());
        if (value instanceof Number || Values.isNumber(value.toString()))
            this.checkValue = getFunctionFromObject(Values.parseAsInt(value.toString()));
        else
            this.checkValue = tryInitFunction(value.toString(), varMap)
                    .get();

        String _check = section.getAs("checker");
        this.check = tryInitFunction(_check, varMap)
                .validateTypeOrThrow(Boolean.class, Values::isBool);

        section.ifValuePresent("taker", String.class, _taker -> this.taker = tryInitFunction(_taker, varMap).validateTypeOrThrow(Boolean.class, Values::isBool));
        section.ifValuePresent("display", String.class, _display -> this.displayName = _display);

        section.ifSectionPresent("taker", takerSection -> {
            takerSection.ensureHasValues("type");
            String type = takerSection.getAs("type");
            if (type.equalsIgnoreCase("command")) {
                takerSection.ensureHasValues("command");
                String command = takerSection.getAs("command");

                Data data = new Data();
                data.add("prisoner", SPrisoner.class);

                command = varMap.initializeVariables(command, data);

                String finalCommand = command;
                taker = new Function<Boolean>() {
                    @Override
                    public void initialize(String string, GlobalVariableMap variableMap) {
                    }

                    @Override
                    public Class<Boolean> getType() {
                        return Boolean.class;
                    }

                    @Override
                    public boolean isCacheable() {
                        return false;
                    }

                    @Override
                    public Boolean execute(GlobalVariableMap globalVariables) {
                        String s = globalVariables.extractVariables(finalCommand);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
                        return true;
                    }

                    @Override
                    public String getId() {
                        return "ExecuteCommand";
                    }
                };
            }
        });
    }
}
