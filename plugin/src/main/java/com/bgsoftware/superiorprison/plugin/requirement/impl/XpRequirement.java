package com.bgsoftware.superiorprison.plugin.requirement.impl;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.api.requirement.RequirementHandler;
import com.bgsoftware.superiorprison.plugin.util.XPUtil;

public class XpRequirement implements Requirement {

    private final RequirementHandler<RequirementData> handlder = new RequirementHandler<RequirementData>() {
        @Override
        public boolean testIO(Prisoner prisoner, RequirementData requirementData) throws RequirementException {
            int currentXp = XPUtil.getTotalExperience(prisoner.getPlayer());
            int requiredXp = Integer.parseInt(requirementData.getValue());

            if (currentXp < requiredXp)
                throw new RequirementException(requirementData, currentXp);

            return currentXp >= requiredXp;
        }

        @Override
        public void take(Prisoner prisoner, RequirementData requirementData) {
            int currentXp = XPUtil.getTotalExperience(prisoner.getPlayer());
            int requiredXp = Integer.parseInt(requirementData.getValue());

            XPUtil.setTotalExperience(prisoner.getPlayer(), (currentXp - requiredXp));
        }

        @Override
        public int getPercentage(Prisoner prisoner, RequirementData requirementData) {
            int currentXp = XPUtil.getTotalExperience(prisoner.getPlayer());
            int requiredXp = Integer.parseInt(requirementData.getValue());
            return Math.min((int) Math.round(currentXp * 100.0 / requiredXp), 100);
        }

        @Override
        public String getCurrent(Prisoner prisoner, RequirementData requirementData) {
            return "" + XPUtil.getTotalExperience(prisoner.getPlayer());
        }
    };

    @Override
    public RequirementHandler getHandler() {
        return handlder;
    }

    @Override
    public String getId() {
        return "XP";
    }
}
