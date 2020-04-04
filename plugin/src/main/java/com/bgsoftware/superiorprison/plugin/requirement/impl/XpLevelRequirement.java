package com.bgsoftware.superiorprison.plugin.requirement.impl;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.api.requirement.RequirementHandler;
import com.bgsoftware.superiorprison.plugin.util.XPUtil;

public class XpLevelRequirement implements Requirement {

    private final RequirementHandler<RequirementData> handler = new RequirementHandler<RequirementData>() {
        @Override
        public boolean testIO(Prisoner prisoner, RequirementData requirementData) throws RequirementException {
            if (prisoner.getPlayer().getLevel() < Integer.parseInt(requirementData.getValue()))
                throw new RequirementException(requirementData, prisoner.getPlayer().getLevel());

            return true;
        }

        @Override
        public void take(Prisoner prisoner, RequirementData requirementData) {
            int currentXpAtLevel = XPUtil.getTotalExperience(prisoner.getPlayer());
            int requiredXpLevel = XPUtil.getTotalExperience(Integer.parseInt(requirementData.getValue()));

            XPUtil.setTotalExperience(prisoner.getPlayer(), (currentXpAtLevel - requiredXpLevel));
        }
    };

    @Override
    public RequirementHandler getHandler() {
        return handler;
    }

    @Override
    public String getId() {
        return "XP_LEVEL";
    }
}
