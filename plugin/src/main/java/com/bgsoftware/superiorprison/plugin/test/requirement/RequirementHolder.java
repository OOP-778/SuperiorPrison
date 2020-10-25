package com.bgsoftware.superiorprison.plugin.test.requirement;

import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Getter
public class RequirementHolder implements Cloneable {
    private List<HoldingData> holdingData = new LinkedList<>();
    private GlobalVariableMap variableMap;

    public RequirementHolder() {}

    public RequirementHolder(GlobalVariableMap scriptVars) {
        this.variableMap = scriptVars;
    }

    protected void add(RequirementData data, Requirement req) {
        holdingData.add(new HoldingData(data, req));
    }

    public void take(GlobalVariableMap clone) {
        for (HoldingData holdingDatum : holdingData) {
            if (holdingDatum.getData().getTaker() != null)
                holdingDatum.getData().getTaker().execute(clone);
        }
    }

    @Getter
    @RequiredArgsConstructor
    public class HoldingData {
        private final RequirementData data;
        private final Requirement requirement;
    }

    public boolean meets(GlobalVariableMap variableMap) {
        boolean[] meets = new boolean[]{true};
        for (HoldingData holdingDatum : holdingData) {
            if (!meets[0]) return false;

            meets[0] = holdingDatum.getRequirement().test(holdingDatum.getData(), variableMap);
        }
        return meets[0];
    }

    public RequirementHolder clone() {
        RequirementHolder holder = new RequirementHolder();
        holder.getHoldingData().addAll(holdingData);

        return holder;
    }
}
