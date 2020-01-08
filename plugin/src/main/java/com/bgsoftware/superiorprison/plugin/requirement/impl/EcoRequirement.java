package com.bgsoftware.superiorprison.plugin.requirement.impl;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementHandler;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;

public class EcoRequirement implements Requirement {

    VaultHook vaultHook = SuperiorPrisonPlugin.getInstance().getHookController().findHook(VaultHook.class).get();

    private RequirementHandler<RequirementData> handler = new RequirementHandler<RequirementData>() {
        @Override
        public boolean test(Prisoner prisoner, RequirementData requirementData) {
            double balance = vaultHook.getEcoProvider().getBalance(prisoner.getOfflinePlayer());
            return balance >= Double.parseDouble(requirementData.getValue());
        }

        @Override
        public void take(Prisoner prisoner, RequirementData requirementData) {
            double take = Double.parseDouble(requirementData.getValue());
            vaultHook.getEcoProvider().withdrawPlayer(prisoner.getOfflinePlayer(), take);
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
