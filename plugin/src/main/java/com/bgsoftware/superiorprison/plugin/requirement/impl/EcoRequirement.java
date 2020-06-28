package com.bgsoftware.superiorprison.plugin.requirement.impl;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.api.requirement.RequirementHandler;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;

public class EcoRequirement implements Requirement {
    private static final RequirementHandler<RequirementData> handler = new RequirementHandler<RequirementData>() {
        final VaultHook vaultHook = SuperiorPrisonPlugin.getInstance().getHookController().findHook(() -> VaultHook.class).get();

        @Override
        public boolean testIO(Prisoner prisoner, RequirementData requirementData) throws RequirementException {
            double balance = vaultHook.getEcoProvider().getBalance(prisoner.getOfflinePlayer());
            if (balance < Double.parseDouble(requirementData.getValue()))
                throw new RequirementException(requirementData, balance);

            return true;
        }

        @Override
        public void take(Prisoner prisoner, RequirementData requirementData) {
            double take = Double.parseDouble(requirementData.getValue());
            vaultHook.getEcoProvider().withdrawPlayer(prisoner.getOfflinePlayer(), take);
        }

        @Override
        public int getPercentage(Prisoner prisoner, RequirementData requirementData) {
            double balance = vaultHook.getEcoProvider().getBalance(prisoner.getOfflinePlayer());
            double required = Double.parseDouble(requirementData.getValue());
            return (int) Math.min(balance * 100 / required, 100);
        }
    };

    @Override
    public RequirementHandler getHandler() {
        return handler;
    }

    @Override
    public String getId() {
        return "ECO";
    }
}
