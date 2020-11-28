package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.ExpireableCache;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class LadderHelper {
    private static final ExpireableCache<UUID, Boolean> cache = new ExpireableCache<>();

    public static void cooldown(UUID uuid, long seconds) {
        cache.put(uuid, true, seconds);
    }

    public static boolean checkForCooldown(Player player) {
        OPair<Boolean, Long> pair = cache.getPair(player.getUniqueId());
        if (pair != null) {
            messageBuilder(LocaleEnum.PRISONER_LADDERUP_COOLDOWN
                    .getWithErrorPrefix())
                    .replace("{cooldown}", TimeUtil.toString(TimeUnit.MILLISECONDS.toSeconds(Duration.between(Instant.now(), Instant.ofEpochSecond(pair.getSecond())).toMillis())))
                    .send(player);
            return false;
        }

        return true;
    }

    @AllArgsConstructor
    public static class MaxRankResult {
        private ParsedObject last;
        private int timesRankedUp;
        private List<String> commands;
    }

    public static MaxRankResult doMaxRank(SPrisoner prisoner, int currentRank, int maxRank, ParsedObject parsedObject) {
        ParsedObject last = null;
        int startingRank = currentRank;

        List<String> commands = new ArrayList<>();
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

            // Add all cmds
            commands.addAll(parsedObject.getCommands());

            // End
            last = parsed;
        }

        return new MaxRankResult(last, currentRank - startingRank, commands);
    }

    public static List<String> mergeCommands(List<String> commands) {
        Map<String, Integer> duplicates = new HashMap<>();

        String[] strings = commands.toArray(new String[0]);
        for (int i = strings.length - 1; i >= 0; i--) {
            String command = strings[i];
            duplicates.merge(command, 1, Integer::sum);
        }

        List<String> newCommands = new ArrayList<>();
        for (Map.Entry<String, Integer> duplicateEntry : duplicates.entrySet()) {
            String key = duplicateEntry.getKey();
            newCommands.add(StringUtils.replace(key, "{}", duplicateEntry.getValue() + ""));
        }

        return newCommands;
    }
}
