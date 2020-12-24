package com.bgsoftware.superiorprison.plugin.commands.ladder;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.ExpireableCache;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.google.common.collect.Sets;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class LadderHelper {
    private static final ExpireableCache<UUID, Boolean> cache = new ExpireableCache<>();
    private static final Set<UUID> peopleRunningLadderCmd = Sets.newConcurrentHashSet();

    public static void cooldown(UUID uuid, long seconds) {
        cache.put(uuid, true, seconds);
    }

    public static boolean checkForCooldown(Player player) {
        OPair<Boolean, Long> pair = cache.getPair(player.getUniqueId());
        if (pair != null) {
            messageBuilder(LocaleEnum.PRISONER_LADDERUP_COOLDOWN
                    .getWithErrorPrefix())
                    .replace("{cooldown}", TimeUtil.toString(Duration.between(Instant.now(), Instant.ofEpochSecond(pair.getSecond())).toMillis() / 1000d))
                    .send(player);
            return false;
        }

        return true;
    }

    public static void addPeopleRunningLadderCmd(UUID uuid) {
        peopleRunningLadderCmd.add(uuid);
    }

    public static boolean isRunningLadderCmd(UUID uuid) {
        return peopleRunningLadderCmd.contains(uuid);
    }

    public static void removeFromRunningLadderCmd(UUID uuid) {
        peopleRunningLadderCmd.remove(uuid);
    }

    @AllArgsConstructor
    @Getter
    public static class MaxRankResult {
        private final ParsedObject last;
        private final BigInteger timesRankedUp;
        private final List<String> commands;
    }

    public static MaxRankResult doMaxRank(SPrisoner prisoner, BigInteger currentRank, BigInteger maxRank, ParsedObject parsedObject) {
        ParsedObject last = null;
        BigInteger startingRank = currentRank;

        List<String> commands = new ArrayList<>();
        while (!NumberUtil.equals(maxRank, currentRank)) {
            currentRank = currentRank.add(BigInteger.ONE);
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

        return new MaxRankResult(last, currentRank.subtract(startingRank).subtract(BigInteger.ONE).max(BigInteger.ZERO), commands);
    }

    private static final Pattern CMD_NUM_PLACEHOLDER = Pattern.compile("@[0-9]+");

    public static List<String> mergeCommands(List<String> commands) {
        List<String> commandsWithoutBrackets = new ArrayList<>();
        Map<String, Integer> duplicates = new HashMap<>();

        String[] strings = commands.toArray(new String[0]);
        for (int i = strings.length - 1; i >= 0; i--) {
            String command = strings[i];
            if (StringUtils.contains(command, "@"))
                duplicates.merge(command, 1, Integer::sum);
            else
                commandsWithoutBrackets.add(command);
        }

        List<String> newCommands = new ArrayList<>();
        for (Map.Entry<String, Integer> duplicateEntry : duplicates.entrySet()) {
            String key = duplicateEntry.getKey();
            newCommands.add(CMD_NUM_PLACEHOLDER.matcher(key).replaceAll(duplicateEntry.getValue() + ""));
        }

        newCommands.addAll(commandsWithoutBrackets);
        return newCommands;
    }
}
