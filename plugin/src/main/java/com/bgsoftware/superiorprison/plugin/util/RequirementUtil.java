package com.bgsoftware.superiorprison.plugin.util;

import com.bgsoftware.superiorprison.api.requirement.Requirement;
import com.bgsoftware.superiorprison.api.requirement.RequirementData;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.ProgressionScaleSection;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;

import java.util.*;

public class RequirementUtil {
    public static List<RequirementException> test(Collection<RequirementData> reqColl, SPrisoner prisoner) {
        List<RequirementException> failed = new ArrayList<>();
        reqColl.forEach(data -> {
            Optional<Requirement> requirement = SuperiorPrisonPlugin.getInstance().getRequirementController().findRequirement(data.getType());
            if (!requirement.isPresent()) return;
            try {
                requirement.get().getHandler().testIO(prisoner, data);
            } catch (RequirementException ex) {
                failed.add(ex);
            }
        });
        return failed;
    }

    public static void take(Collection<RequirementData> reqColl, SPrisoner prisoner) {
        reqColl.forEach(data -> {
            Optional<Requirement> requirement = SuperiorPrisonPlugin.getInstance().getRequirementController().findRequirement(data.getType());
            if (!requirement.isPresent()) return;
            if (!data.isTake()) return;

            requirement.get().getHandler().take(prisoner, data);
        });
    }

    public static int getPercentageCompleted(Collection<RequirementData> reqColl, SPrisoner prisoner) {
        double allReqsPercentage = reqColl.stream().mapToDouble(data -> {
            Optional<Requirement> requirement = SuperiorPrisonPlugin.getInstance().getRequirementController().findRequirement(data.getType());
            if (!requirement.isPresent()) return 0d;
            return Math.min(requirement.get().getHandler().getPercentage(prisoner, data), 100);
        }).sum();

        return Math.min((int) Math.round(allReqsPercentage / reqColl.size()), 100);
    }

    public static String getProgressScale(SPrisoner prisoner, Collection<RequirementData> reqList) {
        ProgressionScaleSection section = SuperiorPrisonPlugin.getInstance().getMainConfig().getScaleSection();
        StringBuilder scaleBuilder = new StringBuilder(section.getCompletedColor());

        char symbol = section.getSymbols().toCharArray()[0];
        int amountShouldBeColored = getPercentageCompleted(reqList, prisoner) / section.getSymbols().toCharArray().length;
        for (int i = 0; i < amountShouldBeColored; i++)
            scaleBuilder.append(symbol);

        scaleBuilder.append(section.getColor());
        for (int i = 0; i < (section.getSymbols().toCharArray().length - amountShouldBeColored); i++)
            scaleBuilder.append(symbol);

        return scaleBuilder.toString();
    }

}
