package com.bgsoftware.superiorprison.plugin.requirement;

import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.requirement.impl.*;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Set;

public class RequirementRegisterer {
    private final Set<Requirement> reqs = Sets.newHashSet();

    public RequirementRegisterer() {
        add(new XpLevelRequirement(), new XpRequirement(), new RankRequirement());
        SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> PapiHook.class, () -> add(new PapiRequirement()));
        SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> VaultHook.class, () -> add(new EcoRequirement()));

        register();
    }

    private void add(Requirement... rq) {
        reqs.addAll(Arrays.asList(rq));
    }

    private void register() {
        reqs.forEach(SuperiorPrisonPlugin.getInstance().getRequirementController()::registerRequirement);
    }
}
