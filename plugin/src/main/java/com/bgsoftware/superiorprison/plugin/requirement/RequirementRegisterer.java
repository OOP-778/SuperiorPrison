package com.bgsoftware.superiorprison.plugin.requirement;

import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.requirement.impl.PapiRequirement;
import com.bgsoftware.superiorprison.plugin.requirement.impl.XpLevelRequirement;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Set;

public class RequirementRegisterer {

    private Set<Requirement> reqs = Sets.newHashSet();

    public RequirementRegisterer() {
        add(new PapiRequirement(), new XpLevelRequirement());
        register();
    }

    void add(Requirement... rq) {
        reqs.addAll(Arrays.asList(rq));
    }

    void register() {
        reqs.forEach(SuperiorPrisonPlugin.getInstance().getRequirementController()::registerRequirement);
    }

}
