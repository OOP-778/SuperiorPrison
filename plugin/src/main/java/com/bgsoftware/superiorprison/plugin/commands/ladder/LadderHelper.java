package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.oop.orangeengine.main.util.data.pair.OPair;

public class LadderHelper {
    public static OPair<ParsedObject, Integer> doMaxRank(SPrisoner prisoner, int currentRank, int maxRank, ParsedObject parsedObject) {
        ParsedObject last = null;

        int startingRank = currentRank;

        while (currentRank != maxRank) {
            currentRank += 1;
            ParsedObject parsed = last == null
                    ? parsedObject
                    : (ParsedObject) last.getNext().orElse(null);
            if (parsed == null) break;

            // Check if prisoner meets requirements
            if (!parsed.getMeets().get()) break;

            // Take requirements from the prisoner
            parsed.take();

            // Set the current ladder rank
            prisoner._setLadderRank(parsed.getIndex());

            // End
            last = parsed;
        }

        return new OPair<>(last, currentRank - startingRank);
    }
}
