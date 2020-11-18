package com.bgsoftware.superiorprison.plugin.requirement;

import com.bgsoftware.superiorprison.plugin.util.script.util.Values;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper;
import com.oop.orangeengine.main.util.data.pair.OPair;

public class Requirement {
    public OPair<Boolean, DeclinedRequirement> test(RequirementData data, GlobalVariableMap map) {
        Object getter = data.getGetter().execute(map);
        Object value = data.getCheckValue().execute(map);

        map.newOrReplace("%getter%", VariableHelper.createVariable(getter));
        map.newOrReplace("%value%", VariableHelper.createVariable(value));

        //Bukkit.broadcastMessage(data.getDisplayName() + " ~ " + getter + " ~ " + value);

        boolean succeed = data.getCheck().execute(map);
        return new OPair<>(succeed, !succeed ? new DeclinedRequirement(null, getter, value) : null);
    }

    public boolean take(RequirementData data, GlobalVariableMap map) {
        Object getter = data.getGetter().execute(map);
        Object value = data.getCheckValue().execute(map);

        map.newOrReplace("%getter%", VariableHelper.createVariable(getter));
        map.newOrReplace("%value%", VariableHelper.createVariable(value));

        //Bukkit.broadcastMessage(data.getDisplayName() + " ~ " + getter + " ~ " + value);

        if (data.getTaker() != null)
            return data.getTaker().executeWithTiming(map);

        return true;
    }

    public int getPercentage(RequirementData data, GlobalVariableMap map) {
        Object getter = data.getGetter().execute(map);
        Object value = data.getCheckValue().execute(map);

        if (Values.isNumber(getter) && Values.isNumber(value)) {
            Number[] numbers = Values.parseNumbers(getter.toString(), value.toString());
            int _getter = numbers[0].intValue();
            int _value = numbers[1].intValue();

            return Math.min(100, (_getter / _value) * 100);
        }

        return 100;
    }

    public String getCurrent(RequirementData data, GlobalVariableMap map) {
        return data.getGetter().execute(map).toString();
    }
}
