package com.bgsoftware.superiorprison.plugin.test.requirement;

import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper;

public class Requirement {
    public boolean test(RequirementData data, GlobalVariableMap map) {
        Object execute = data.getGetter().execute(map);
        Object execute1 = data.getCheckValue().execute(map);

        map.newOrReplace("%getter%", VariableHelper.createVariable(execute));
        map.newOrReplace("%value%", VariableHelper.createVariable(execute1));
        return data.getCheck().execute(map);
    }

    public boolean take(RequirementData data, GlobalVariableMap map) {
        if (data.getTaker() != null)
            return data.getTaker().execute(map);

        return true;
    }

    public int getPercentage(RequirementData data, GlobalVariableMap map) {
        return 0;
    }

    public String getCurrent(RequirementData data, GlobalVariableMap map) {
        return data.getGetter().execute(map).toString();
    }
}
