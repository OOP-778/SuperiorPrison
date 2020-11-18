package com.bgsoftware.superiorprison.plugin.util.placeholders;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
import com.bgsoftware.superiorprison.api.data.player.LadderObject;
import com.bgsoftware.superiorprison.api.data.statistic.StatisticsContainer;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.statistic.SBlocksStatistic;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer;
import com.bgsoftware.superiorprison.plugin.ladder.ParsedObject;
import com.bgsoftware.superiorprison.plugin.util.RequirementUtil;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.bgsoftware.superiorprison.plugin.util.placeholders.parser.ArgsCrawler;
import com.bgsoftware.superiorprison.plugin.util.placeholders.parser.ObjectCache;
import com.bgsoftware.superiorprison.plugin.util.placeholders.parser.Parser;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Location;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.bgsoftware.superiorprison.plugin.util.TimeUtil.getDate;

public class PlaceholderParser {
    private static final Parser<Object, Object> parser = new Parser<>()
            // Prisoner placeholders
            .add("prisoner", SPrisoner.class)
            .mapper((none, none0, crawler) -> {
                String identifier = crawler.hasNext() ? crawler.next() : "";
                if (identifier.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"))
                    return (SPrisoner) SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(UUID.fromString(identifier)).orElse(null);

                else
                    return identifier.trim().length() == 0 ? null : (SPrisoner) SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(identifier).orElse(null);
            })
            .parse("autosell", prisoner -> booleanToState(prisoner.isAutoSell()))
            .parse("autopickup", prisoner -> booleanToState(prisoner.isAutoPickup()))
            .parse("autoburn", prisoner -> booleanToState(prisoner.isAutoBurn()))
            .parse("currentmine", prisoner -> prisoner.getCurrentMine().map(Pair::getKey).map(SuperiorMine::getName).orElse("none"))
            .parse("fortuneblocks", prisoner -> booleanToState(prisoner.isFortuneBlocks()))
            .parse("canenter", (prisoner, obj, crawler) -> canEnter(prisoner, crawler))
            .parse("rankupscale/ladderscale", (prisoner, obj, crawler) -> {
                Optional<LadderObject> nextLadderRank = prisoner.getParsedLadderRank().getNext();
                if (!nextLadderRank.isPresent()) {
                    Optional<LadderObject> currentPrestige = prisoner.getParsedPrestige();
                    LadderObject nextPrestige;
                    if (currentPrestige.isPresent())
                        nextPrestige = currentPrestige.get().getNext().orElse(null);
                    else
                        nextPrestige = SuperiorPrisonPlugin.getInstance().getPrestigeController().getParsed(prisoner, 1).get();
                    if (nextPrestige == null)
                        return SuperiorPrisonPlugin.getInstance().getMainConfig().getPlaceholdersSection().getPrestigeNotFound();

                    return RequirementUtil.getProgressScale((ParsedObject) nextPrestige);
                }

                return nextLadderRank
                        .map(lo -> RequirementUtil.getProgressScale((ParsedObject) lo));
            })
            .parse("rankuppercentage/ladderpercentage", (prisoner, obj, crawler) -> {
                Optional<LadderObject> nextLadderRank = prisoner.getParsedLadderRank().getNext();
                if (!nextLadderRank.isPresent()) {
                    Optional<LadderObject> currentPrestige = prisoner.getParsedPrestige();
                    LadderObject nextPrestige;
                    if (currentPrestige.isPresent())
                        nextPrestige = currentPrestige.get().getNext().orElse(null);
                    else
                        nextPrestige = SuperiorPrisonPlugin.getInstance().getPrestigeController().getParsed(prisoner, 1).get();
                    if (nextPrestige == null)
                        return SuperiorPrisonPlugin.getInstance().getMainConfig().getPlaceholdersSection().getPrestigeNotFound();

                    return RequirementUtil.getPercentageCompleted((ParsedObject) nextPrestige);
                }

                return nextLadderRank
                        .map(lo -> RequirementUtil.getPercentageCompleted((ParsedObject) lo));
            })

            .add("prestige", LadderObject.class)
            .mapper((none, prisoner, crawler) -> prisoner.getParsedPrestige().orElse(null))
            .parse("prefix/order/name", (prestige, prisoner, crawler) -> getFromAccess(prestige, crawler.current(), false))

            .add("next", LadderObject.class)
            .mapper((none, prestige, crawler) -> prestige.getNext().orElse(null))
            .parse("prefix/order/name/index", (prestige, none, crawler) -> getFromAccess(prestige, crawler.current(), false))
            .parent(LadderObject.class, SPrisoner.class)

            .add("previous", LadderObject.class)
            .mapper((none, prestige, crawler) -> (LadderObject) prestige.getPrevious().orElse(null))
            .parse("prefix/order/name/index", (prestige, none, crawler) -> getFromAccess(prestige, crawler.current(), false))
            .parent(LadderObject.class, SPrisoner.class)

            .parent(SPrisoner.class, Object.class)
            .add("ladderrank", LadderObject.class)
            .mapper((none, prisoner, crawler) -> prisoner.getParsedLadderRank())
            .parse("prefix/order/name/index", (rank, none, crawler) -> getFromAccess(rank, crawler.current(), true))

            .add("next", LadderObject.class)
            .mapper((none, rank, crawler) -> rank.getNext().orElse(null))
            .parse("prefix/order/name/index", (rank, none, crawler) -> getFromAccess(rank, crawler.current(), true))
            .parent(LadderObject.class, SPrisoner.class)

            .add("previous", LadderObject.class)
            .mapper((none, rank, crawler) -> rank.getPrevious().orElse(null))
            .parse("prefix/order/name/index", (rank, none, crawler) -> getFromAccess(rank, crawler.current(), true))
            .parent(LadderObject.class, SPrisoner.class)

            .parent(SPrisoner.class, Object.class)

            .add("statistics", SStatisticsContainer.class)
            .mapper((none, prisoner, crawler) -> SuperiorPrisonPlugin.getInstance().getStatisticsController().getContainer(prisoner.getUUID()))
            .add("blocks", SBlocksStatistic.class)
            .mapper((none, statistics, crawler) -> statistics.getBlocksStatistic())
            .parse("total", (statistic, crawler) -> {
                if (crawler.hasNext()) {
                    return statistic.get(OMaterial.matchMaterial(crawler.next()));

                } else
                    return statistic.getTotal();
            })

            // Placeholder: withinTime_start_end
            .parse("withinTime", (statistic, crawler) -> {
                if (crawler.hasNext()) {
                    ZonedDateTime start = getDate();
                    ZonedDateTime end = getDate();
                    if (crawler.hasNext()) {
                        long seconds = TimeUtil.toSeconds(crawler.next());
                        start = start.minusSeconds(seconds);
                        if (crawler.hasNext())
                            return statistic.getBlockWithinTimeFrame(start, end, OMaterial.matchMaterial(crawler.next()));
                    }

                    return statistic.getTotalBlocksWithinTimeFrame(start, end);
                } else
                    return null;
            })
            .parent(StatisticsContainer.class, SPrisoner.class)
            .parent(SPrisoner.class, Object.class)
            .parent(Object.class, Object.class)

            // Mine Placeholders
            .add("mine", SNormalMine.class)
            .mapper((none, none0, crawler) -> crawler.hasNext() ? (SNormalMine) SuperiorPrisonPlugin.getInstance().getMineController().getMine(crawler.next()).orElse(null) : null)
            .parse("type", mine -> Helper.beautify(mine.getType().name()))
            .parse("prisonercount", mine -> mine.getPrisoners().size())

            .add("spawnpoint", Location.class)
            .mapper((none, mine, crawler) -> mine.getSpawnPoint())
            .parse("world/x/y/z", (location, crawler) -> getFromLocation(location, crawler.current()))
            .parent(SNormalMine.class, Location.class)

            .add("settings", SMineSettings.class)
            .mapper((none, mine, crawler) -> mine.getSettings())
            .parse("limit", SMineSettings::getPlayerLimit)
            .add("reset", ResetSettings.class)
            .mapper((none, settings, crawler) -> settings.getResetSettings())
            .parse("type", reset -> reset.getType().name().toLowerCase())
            .parse("value", ResetSettings::getValueHumanified)
            .parse("current", ResetSettings::getCurrentHumanified)

            .parent(ResetSettings.class, SMineSettings.class)
            .parent(SMineSettings.class, SNormalMine.class)
            .parent(Object.class, Object.class);

    public static String parse(String[] split, ObjectCache cache) {
        return parser.parse(split, cache);
    }

    private static String canEnter(SPrisoner prisoner, ArgsCrawler crawler) {
        if (crawler.hasNext()) {
            String next = crawler.next();
            SuperiorMine mine = SuperiorPrisonPlugin.getInstance().getMineController().getMine(next).orElse(null);
            if (mine == null) return "Invalid Mine";

            return mine.canEnter(prisoner) ? "Yes" : "No";
        }
        return "Mine Not Set";
    }

    private static String getFromLocation(Location location, String identifier) {
        if (identifier == null)
            return null;

        if (identifier.equalsIgnoreCase("world"))
            return location.getWorld().getName();

        if (identifier.equalsIgnoreCase("x"))
            return location.getBlockX() + "";

        if (identifier.equalsIgnoreCase("y"))
            return location.getBlockY() + "";

        if (identifier.equalsIgnoreCase("z"))
            return location.getBlockZ() + "";

        return "Invalid identifier";
    }

    private static String getFromAccess(LadderObject ob, String identifier, boolean rank) {
        if (ob == null)
            return !rank ? SuperiorPrisonPlugin.getInstance().getMainConfig().getPlaceholdersSection().getPrestigeNotFound() : SuperiorPrisonPlugin.getInstance().getMainConfig().getPlaceholdersSection().getRankNotFound();
        if (identifier == null)
            return ob.getName();

        if (identifier.equalsIgnoreCase("prefix"))
            return ob.getPrefix();

        if (identifier.equalsIgnoreCase("order") || identifier.equalsIgnoreCase("index"))
            return ob.getIndex() + "";

        if (identifier.equalsIgnoreCase("name"))
            return ob.getName();

        return "invalid identifier";
    }

    private static Object booleanToState(boolean bool) {
        return bool ? "enabled" : "disabled";
    }
}
