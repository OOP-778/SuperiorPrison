package com.bgsoftware.superiorprison.plugin.requirement.impl;

import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.api.requirement.RequirementHandler;

public class RankRequirement implements Requirement {
    private static final RequirementHandler<RequirementData> handler = (prisoner, requirementData) -> {
        boolean has = prisoner.hasRank(requirementData.getValue());
        if (!has)
            throw new RequirementException(requirementData, prisoner.getCurrentLadderRank().getName());
        return true;
    };

    @Override
    public RequirementHandler getHandler() {
        return handler;
    }

    @Override
    public String getId() {
        return "RANK";
    }
}
