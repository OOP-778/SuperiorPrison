package com.bgsoftware.superiorprison.plugin.test.requirement;

import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
public class RequirementHolder implements Cloneable {
    private List<HoldingData> holdingData = new LinkedList<>();

    public RequirementHolder() {}

    protected void add(RequirementData data, Requirement req) {
        holdingData.add(new HoldingData(data, req));
    }

    public void take(GlobalVariableMap clone) {
        for (HoldingData holdingDatum : holdingData) {
            if (holdingDatum.getData().getTaker() != null)
                holdingDatum.getData().getTaker().execute(clone);
        }
    }

    public int getPercentageCompleted(GlobalVariableMap variableMap) {
        double totalCompleted = 0;
        for (HoldingData holdingDatum : holdingData)
            totalCompleted += holdingDatum.getRequirement().getPercentage(holdingDatum.getData(), variableMap);

        return Math.min((int) totalCompleted / holdingData.size(), 100);
    }

    @Getter
    @RequiredArgsConstructor
    public class HoldingData {
        private final RequirementData data;
        private final Requirement requirement;
    }

    public OPair<Boolean, List<DeclinedRequirement>> meets(GlobalVariableMap variableMap) {
        List<DeclinedRequirement> declinedRequirements = new ArrayList<>();

        boolean[] meets = new boolean[]{true};
        for (HoldingData holdingDatum : holdingData) {
            OPair<Boolean, DeclinedRequirement> test = holdingDatum.getRequirement().test(holdingDatum.getData(), variableMap);

            // If the declined value is not null
            if (test.getSecond() != null) {
                test.getSecond().setDisplay(holdingDatum.data.getDisplayName() == null ? "Not Set" : holdingDatum.data.getDisplayName());
                declinedRequirements.add(test.getSecond());
            }

            if (!test.getFirst())
                meets[0] = false;
        }

        return new OPair<>(meets[0], declinedRequirements);
    }

    public RequirementHolder clone() {
        RequirementHolder holder = new RequirementHolder();
        holder.getHoldingData().addAll(holdingData);

        return holder;
    }
}