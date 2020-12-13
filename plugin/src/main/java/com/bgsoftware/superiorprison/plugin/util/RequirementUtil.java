package com.bgsoftware.superiorprison.plugin.util;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.main.ProgressionScaleSection;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;

public class RequirementUtil {
    public static int getPercentageCompleted(ParsedObject parsedObject) {
        return parsedObject.getTemplate().getRequirements().getPercentageCompleted(parsedObject.getVariableMap());
    }

    public static String getProgressScale(ParsedObject parsedObject) {
        ProgressionScaleSection section = SuperiorPrisonPlugin.getInstance().getMainConfig().getScaleSection();
        StringBuilder scaleBuilder = new StringBuilder(section.getCompletedColor());

        int len = section.getSymbols().length();
        char symbol = section.getSymbols().charAt(0);

        int amountShouldBeColored = getPercentageCompleted(parsedObject) / len;
        for (int i = 0; i < amountShouldBeColored; i++)
            scaleBuilder.append(symbol);

        scaleBuilder.append(section.getColor());
        for (int i = 0; i < (len - amountShouldBeColored); i++)
            scaleBuilder.append(symbol);

        return scaleBuilder.toString();
    }

}
