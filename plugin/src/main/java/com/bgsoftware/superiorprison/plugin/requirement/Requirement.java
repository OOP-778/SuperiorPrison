package com.bgsoftware.superiorprison.plugin.requirement;

import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.bgsoftware.superiorprison.plugin.util.script.util.Values;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.Arrays;
import java.util.Objects;

public class Requirement {
    public OPair<Boolean, DeclinedRequirement> test(RequirementData data, GlobalVariableMap map) {
        Object getter = data.getGetter().execute(map);
        Object value = data.getCheckValue().execute(map);

        map.newOrReplace("%getter%", VariableHelper.createVariable(getter));
        map.newOrReplace("%value%", VariableHelper.createVariable(value));

        boolean succeed = data.getCheck().execute(map);
        return new OPair<>(succeed, !succeed ? new DeclinedRequirement(null, getter, value) : null);
    }

    public boolean take(RequirementData data, GlobalVariableMap map) {
        Object getter = data.getGetter().execute(map);
        Object value = data.getCheckValue().execute(map);

        map.newOrReplace("%getter%", VariableHelper.createVariable(getter));
        map.newOrReplace("%value%", VariableHelper.createVariable(value));

        if (data.getTaker() != null)
            return data.getTaker().execute(map);

        return true;
    }

    public int getPercentage(RequirementData data, GlobalVariableMap map) {
        Object getter = data.getGetter().execute(map);
        Object value = data.getCheckValue().execute(map);

        if (!(getter instanceof Number) || !(value instanceof Number)) {
            Number[] numbers = Values.parseNumbers(getter.toString(), value.toString());
            if (Arrays.stream(numbers).anyMatch(Objects::isNull)) return 100;

            getter = numbers[0];
            value = numbers[1];
        }

        int min = Math.min(100, NumberUtil.getPercentageBetween((Number) getter, (Number) value));
        return min;
    }

    public String getCurrent(RequirementData data, GlobalVariableMap map) {
        return data.getGetter().execute(map).toString();
    }
}
