package com.bgsoftware.superiorprison.plugin.test.requirement;

import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.bgsoftware.superiorprison.plugin.test.script.variable.VariableHelper.createVariable;

public class RequirementHolder {
    private final List<HoldingData> holdingData = new LinkedList<>();
    private final GlobalVariableMap scriptVars = new GlobalVariableMap();

    public RequirementHolder() {

        scriptVars.newOrReplace("rank_order", createVariable(2));
        scriptVars.newOrReplace("rank_order", createVariable(2));
    }

    @Getter
    @RequiredArgsConstructor
    class HoldingData {
        private final AtomicReference<RequirementData> data;
        private Requirement requirement;
    }
}
