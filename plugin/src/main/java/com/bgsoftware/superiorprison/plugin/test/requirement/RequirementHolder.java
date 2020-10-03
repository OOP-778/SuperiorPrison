package com.bgsoftware.superiorprison.plugin.test.requirement;

import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RequirementHolder {
    private final List<HoldingData> holdingData = new LinkedList<>();
    private final GlobalVariableMap scriptVars;

    public RequirementHolder(GlobalVariableMap scriptVars) {
        this.scriptVars = scriptVars;
    }

    @Getter
    @RequiredArgsConstructor
    class HoldingData {
        private final AtomicReference<RequirementData> data;
        private Requirement requirement;
    }
}
