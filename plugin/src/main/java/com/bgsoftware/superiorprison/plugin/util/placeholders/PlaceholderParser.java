package com.bgsoftware.superiorprison.plugin.util.placeholders;

import static com.bgsoftware.superiorprison.plugin.util.TimeUtil.getDate;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.settings.ResetSettings;
import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.data.statistic.StatisticsContainer;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;
import com.bgsoftware.superiorprison.plugin.object.player.SPrestige;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.statistic.SBlocksStatistic;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.bgsoftware.superiorprison.plugin.util.RequirementUtil;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.bgsoftware.superiorprison.plugin.util.placeholders.parser.ArgsCrawler;
import com.bgsoftware.superiorprison.plugin.util.placeholders.parser.ObjectCache;
import com.bgsoftware.superiorprison.plugin.util.placeholders.parser.Parser;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.material.OMaterial;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;

public class PlaceholderParser {
  private static final Parser<Object, Object> parser =
      new Parser<>()
          // Prisoner placeholders
          .add("prisoner", SPrisoner.class)
          .mapper(
              (none, none0, crawler) -> {
                String identifier = crawler.hasNext() ? crawler.next() : "";
                if (identifier.matches(
                    "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"))
                  return (SPrisoner)
                      SuperiorPrisonPlugin.getInstance()
                          .getPrisonerController()
                          .getPrisoner(UUID.fromString(identifier))
                          .orElse(null);
                else
                  return identifier.trim().length() == 0
                      ? null
                      : (SPrisoner)
                          SuperiorPrisonPlugin.getInstance()
                              .getPrisonerController()
                              .getPrisoner(identifier)
                              .orElse(null);
              })
          .parse("autosell", prisoner -> booleanToState(prisoner.isAutoSell()))
          .parse("autopickup", prisoner -> booleanToState(prisoner.isAutoPickup()))
          .parse("autoburn", prisoner -> booleanToState(prisoner.isAutoBurn()))
          .parse(
              "currentmine",
              prisoner ->
                  prisoner
                      .getCurrentMine()
                      .map(Pair::getKey)
                      .map(SuperiorMine::getName)
                      .orElse("none"))
          .parse("fortuneblocks", prisoner -> booleanToState(prisoner.isFortuneBlocks()))
          .parse("canenter", (prisoner, obj, crawler) -> canEnter(prisoner, crawler))
          .parse(
              "nextladderrequirement",
              (prisoner, obj, crawler) -> {
                if (crawler.hasNext()) {
                  String requirementType = crawler.next().toUpperCase();
                  Optional<LadderRank> nextLadderRank = prisoner.getCurrentLadderRank().getNext();
                  if (!nextLadderRank.isPresent()) {
                    SPrestige nextPrestige;
                    if (prisoner.getCurrentPrestige().isPresent())
                      nextPrestige =
                          (SPrestige) prisoner.getCurrentPrestige().get().getNext().orElse(null);
                    else
                      nextPrestige =
                          (SPrestige)
                              SuperiorPrisonPlugin.getInstance()
                                  .getPrestigeController()
                                  .getPrestige(1)
                                  .orElse(null);
                    if (nextPrestige == null)
                      return SuperiorPrisonPlugin.getInstance()
                          .getMainConfig()
                          .getPlaceholdersSection()
                          .getPrestigeNotFound();

                    return nextPrestige.getRequirements().stream()
                        .filter(req -> req.getType().equalsIgnoreCase(requirementType))
                        .map(
                            requirementData ->
                                    TextUtil.beautifyNumber(requirementData.getValue()))
                        .findFirst()
                        .orElse(
                            SuperiorPrisonPlugin.getInstance()
                                .getMainConfig()
                                .getPlaceholdersSection()
                                .getPrestigeNotFound());
                  }

                  SLadderRank next = (SLadderRank) nextLadderRank.get();
                  return next.getRequirements().stream()
                      .filter(req -> req.getType().equalsIgnoreCase(requirementType))
                      .map(
                          requirementData -> TextUtil.beautifyNumber(requirementData.getValue()))
                      .findFirst()
                      .orElse(
                          SuperiorPrisonPlugin.getInstance()
                              .getMainConfig()
                              .getPlaceholdersSection()
                              .getRankNotFound());
                }
                return "Invalid req";
              })
          .parse(
              "rankupscale",
              (prisoner, obj, crawler) -> {
                Optional<LadderRank> nextLadderRank = prisoner.getCurrentLadderRank().getNext();
                if (!nextLadderRank.isPresent()) {
                  SPrestige nextPrestige;
                  if (prisoner.getCurrentPrestige().isPresent())
                    nextPrestige =
                        (SPrestige) prisoner.getCurrentPrestige().get().getNext().orElse(null);
                  else
                    nextPrestige =
                        (SPrestige)
                            SuperiorPrisonPlugin.getInstance()
                                .getPrestigeController()
                                .getPrestige(1)
                                .orElse(null);
                  if (nextPrestige == null)
                    return SuperiorPrisonPlugin.getInstance()
                        .getMainConfig()
                        .getPlaceholdersSection()
                        .getPrestigeNotFound();

                  return RequirementUtil.getProgressScale(prisoner, nextPrestige.getRequirements());
                }

                SLadderRank next = (SLadderRank) nextLadderRank.get();
                return RequirementUtil.getProgressScale(prisoner, next.getRequirements());
              })
          .parse(
              "rankuppercentage",
              (prisoner, obj, crawler) -> {
                Optional<LadderRank> nextLadderRank = prisoner.getCurrentLadderRank().getNext();
                if (!nextLadderRank.isPresent()) {
                  SPrestige nextPrestige;
                  if (prisoner.getCurrentPrestige().isPresent())
                    nextPrestige =
                        (SPrestige) prisoner.getCurrentPrestige().get().getNext().orElse(null);
                  else
                    nextPrestige =
                        (SPrestige)
                            SuperiorPrisonPlugin.getInstance()
                                .getPrestigeController()
                                .getPrestige(1)
                                .orElse(null);
                  if (nextPrestige == null)
                    return SuperiorPrisonPlugin.getInstance()
                        .getMainConfig()
                        .getPlaceholdersSection()
                        .getPrestigeNotFound();

                  return RequirementUtil.getPercentageCompleted(
                      nextPrestige.getRequirements(), prisoner);
                }

                SLadderRank next = (SLadderRank) nextLadderRank.get();
                return RequirementUtil.getPercentageCompleted(next.getRequirements(), prisoner);
              })
          .add("prestige", SPrestige.class)
          .mapper(
              (none, prisoner, crawler) -> (SPrestige) prisoner.getCurrentPrestige().orElse(null))
          .parse(
              "prefix/order/name",
              (prestige, prisoner, crawler) -> getFromAccess(prestige, crawler.current(), false))
          .add("next", SPrestige.class)
          .mapper((none, prestige, crawler) -> (SPrestige) prestige.getNext().orElse(null))
          .parse(
              "prefix/order/name",
              (prestige, none, crawler) -> getFromAccess(prestige, crawler.current(), false))
          .parent(SPrestige.class, SPrisoner.class)
          .add("previous", SPrestige.class)
          .mapper((none, prestige, crawler) -> (SPrestige) prestige.getPrevious().orElse(null))
          .parse(
              "prefix/order/name",
              (prestige, none, crawler) -> getFromAccess(prestige, crawler.current(), false))
          .parent(SPrestige.class, SPrisoner.class)
          .parent(SPrisoner.class, Object.class)
          .add("ladderrank", SLadderRank.class)
          .mapper((none, prisoner, crawler) -> (SLadderRank) prisoner.getCurrentLadderRank())
          .parse(
              "prefix/order/name",
              (rank, none, crawler) -> getFromAccess(rank, crawler.current(), true))
          .add("next", SLadderRank.class)
          .mapper((none, rank, crawler) -> (SLadderRank) rank.getNext().orElse(null))
          .parse(
              "prefix/order/name",
              (rank, none, crawler) -> getFromAccess(rank, crawler.current(), true))
          .parent(SLadderRank.class, SPrisoner.class)
          .add("previous", SLadderRank.class)
          .mapper((none, rank, crawler) -> (SLadderRank) rank.getPrevious().orElse(null))
          .parse(
              "prefix/order/name",
              (rank, none, crawler) -> getFromAccess(rank, crawler.current(), true))
          .parent(SLadderRank.class, SPrisoner.class)
          .parent(SPrisoner.class, Object.class)
          .add(
              "statistics",
              com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer.class)
          .mapper(
              (none, prisoner, crawler) ->
                  SuperiorPrisonPlugin.getInstance()
                      .getStatisticsController()
                      .getContainer(prisoner.getUUID()))
          .add("blocks", SBlocksStatistic.class)
          .mapper((none, statistics, crawler) -> statistics.getBlocksStatistic())
          .parse(
              "total",
              (statistic, crawler) -> {
                if (crawler.hasNext()) {
                  return statistic.get(OMaterial.matchMaterial(crawler.next()));

                } else return statistic.getTotal();
              })

          // Placeholder: withinTime_start_end
          .parse(
              "withinTime",
              (statistic, crawler) -> {
                if (crawler.hasNext()) {
                  ZonedDateTime start = getDate();
                  ZonedDateTime end = getDate();
                  if (crawler.hasNext()) {
                    long seconds = TimeUtil.toSeconds(crawler.next());
                    start = start.minusSeconds(seconds);
                    if (crawler.hasNext())
                      return statistic.getBlockWithinTimeFrame(
                          start, end, OMaterial.matchMaterial(crawler.next()));
                  }

                  return statistic.getTotalBlocksWithinTimeFrame(start, end);
                } else return null;
              })
          .parent(StatisticsContainer.class, SPrisoner.class)
          .parent(SPrisoner.class, Object.class)
          .parent(Object.class, Object.class)

          // Mine Placeholders
          .add("mine", SNormalMine.class)
          .mapper(
              (none, none0, crawler) ->
                  crawler.hasNext()
                      ? (SNormalMine)
                          SuperiorPrisonPlugin.getInstance()
                              .getMineController()
                              .getMine(crawler.next())
                              .orElse(null)
                      : null)
          .parse("type", mine -> Helper.beautify(mine.getType().name()))
          .parse("accessranks", mine -> Arrays.toString(mine.getRanks().toArray()))
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
      SuperiorMine mine =
          SuperiorPrisonPlugin.getInstance().getMineController().getMine(next).orElse(null);
      if (mine == null) return "Invalid Mine";

      return mine.canEnter(prisoner) ? "Yes" : "No";
    }
    return "Mine Not Set";
  }

  private static String getFromLocation(Location location, String identifier) {
    if (identifier == null) return null;

    if (identifier.equalsIgnoreCase("world")) return location.getWorld().getName();

    if (identifier.equalsIgnoreCase("x")) return location.getBlockX() + "";

    if (identifier.equalsIgnoreCase("y")) return location.getBlockY() + "";

    if (identifier.equalsIgnoreCase("z")) return location.getBlockZ() + "";

    return "Invalid identifier";
  }

  private static String getFromAccess(Object object, String identifier, boolean rank) {
    if (object == null)
      return !rank
          ? SuperiorPrisonPlugin.getInstance()
              .getMainConfig()
              .getPlaceholdersSection()
              .getPrestigeNotFound()
          : SuperiorPrisonPlugin.getInstance()
              .getMainConfig()
              .getPlaceholdersSection()
              .getRankNotFound();
    if (identifier == null)
      return object instanceof Rank ? ((Rank) object).getName() : ((Prestige) object).getName();

    if (identifier.equalsIgnoreCase("prefix"))
      return object instanceof Rank ? ((Rank) object).getPrefix() : ((Prestige) object).getPrefix();

    if (identifier.equalsIgnoreCase("order"))
      return object instanceof LadderRank
          ? ((LadderRank) object).getOrder() + ""
          : ((Prestige) object).getOrder() + "";

    if (identifier.equalsIgnoreCase("name"))
      return object instanceof LadderRank
          ? ((LadderRank) object).getName()
          : ((Prestige) object).getName();

    return "invalid identifier";
  }

  private static Object booleanToState(boolean bool) {
    return bool ? "enabled" : "disabled";
  }
}
