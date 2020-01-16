package com.bgsoftware.superiorprison.plugin.requirement.impl;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementHandler;
import com.bgsoftware.superiorprison.plugin.util.XPUtil;

public class XpRequirement implements Requirement {

    private final RequirementHandler<RequirementData> handlder = new RequirementHandler<RequirementData>() {
        @Override
        public boolean test(Prisoner prisoner, RequirementData requirementData) {
            int currentXp = XPUtil.getTotalExperience(prisoner.getPlayer());
            int requiredXp = XPUtil.getTotalExperience(Integer.parseInt(requirementData.getValue()));

            return currentXp >= requiredXp;
        }

        @Override
        public void take(Prisoner prisoner, RequirementData requirementData) {
            int currentXp = XPUtil.getTotalExperience(prisoner.getPlayer());
            int requiredXp = XPUtil.getTotalExperience(Integer.parseInt(requirementData.getValue()));

            XPUtil.setTotalExperience(prisoner.getPlayer(), (currentXp - requiredXp));
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
