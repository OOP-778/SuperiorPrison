package com.bgsoftware.superiorprison.plugin.hook.impl;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.data.statistic.StatisticsContainer;
import com.bgsoftware.superiorprison.api.util.Pair;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.SHook;
import com.bgsoftware.superiorprison.plugin.hook.impl.parser.ObjectCache;
import com.bgsoftware.superiorprison.plugin.hook.impl.parser.PlaceholderParser;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrestige;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.statistic.SBlocksStatistic;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.material.OMaterial;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.bgsoftware.superiorprison.plugin.util.TimeUtil.getDate;

public class PapiHook extends SHook {
    private final PlaceholderParser<Object, Object> parser = new PlaceholderParser<>()
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

            .add("prestige", SPrestige.class)
            .mapper((none, prisoner, crawler) -> (SPrestige) prisoner.getCurrentPrestige().orElse(null))
            .parse("prefix/order/name", (none, prestige, crawler) -> getFromAccess(prestige, crawler.current()))

            .add("next", SPrestige.class)
            .mapper((none, prestige, crawler) -> (SPrestige) prestige.getNext().orElse(null))
            .parse("prefix/order/name", (none, prestige, crawler) -> getFromAccess(prestige, crawler.current()))
            .parent(SPrestige.class, SPrisoner.class)

            .add("previous", SPrestige.class)
            .mapper((none, prestige, crawler) -> (SPrestige) prestige.getPrevious().orElse(null))
            .parse("prefix/order/name", (none, prestige, crawler) -> getFromAccess(prestige, crawler.current()))
            .parent(SPrestige.class, SPrisoner.class)

            .parent(SPrisoner.class, Object.class)
            .add("ladderrank", SLadderRank.class)
            .mapper((none, prisoner, crawler) -> (SLadderRank) prisoner.getCurrentLadderRank())
            .parse("prefix/order/name", (rank, none, crawler) -> getFromAccess(rank, crawler.current()))

            .add("next", SLadderRank.class)
            .mapper((none, rank, crawler) -> (SLadderRank) rank.getNext().orElse(null))
            .parse("prefix/order/name", (rank, none, crawler) -> getFromAccess(rank, crawler.current()))
            .parent(SLadderRank.class, SPrisoner.class)

            .add("previous", SLadderRank.class)
            .mapper((none, rank, crawler) -> (SLadderRank) rank.getPrevious().orElse(null))
            .parse("prefix/order/name", (rank, none, crawler) -> getFromAccess(rank, crawler.current()))
            .parent(SLadderRank.class, SPrisoner.class)

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

            // Placeholder: withinTime_ago_material
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
            .parent(Object.class, Object.class);

    public PapiHook() {
        super(null);
        new Expansion();
    }

    @Override
    public String getPluginName() {
        return "PlaceholderAPI";
    }

    public String parse(Object object, String text) {
        return parse(object instanceof SPrisoner ? ((SPrisoner) object).getOfflinePlayer() : object instanceof OfflinePlayer ? (OfflinePlayer) object : null, text);
    }

    public List<String> parse(Object object, List<String> lore) {
        return parse(object instanceof SPrisoner ? ((SPrisoner) object).getOfflinePlayer() : object instanceof OfflinePlayer ? (OfflinePlayer) object : null, lore);
    }

    public String parse(OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public List<String> parse(OfflinePlayer player, List<String> lore) {
        return PlaceholderAPI.setPlaceholders(player, lore);
    }

    public String getFromLocation(Location location, String identifier) {
        if (identifier.equalsIgnoreCase("world"))
            return location.getWorld().getName();

        else if (identifier.equalsIgnoreCase("x"))
            return location.getBlockX() + "";

        else if (identifier.equalsIgnoreCase("y"))
            return location.getBlockY() + "";

        else if (identifier.equalsIgnoreCase("z"))
            return location.getBlockZ() + "";

        else
            return "invalid identifier";
    }

    public String getFromAccess(Object object, String identifier) {
        if (identifier == null)
            return object instanceof Rank ? ((Rank) object).getName() : ((Prestige) object).getName();

        if (identifier.equalsIgnoreCase("prefix"))
            return object instanceof Rank ? ((Rank) object).getPrefix() : ((Prestige) object).getPrefix();

        if (identifier.equalsIgnoreCase("order"))
            return object instanceof LadderRank ? ((LadderRank) object).getOrder() + "" : ((Prestige) object).getOrder() + "";

        if (identifier.equalsIgnoreCase("name"))
            return object instanceof LadderRank ? ((LadderRank) object).getName() : ((Prestige) object).getName();

        return "invalid identifier";
    }

    private Object booleanToState(boolean bool) {
        return bool ? "enabled" : "disabled";
    }

    public class Expansion extends PlaceholderExpansion {
        public Expansion() {
            register();
        }

        @Override
        public String getIdentifier() {
            return "superiorprison";
        }

        @Override
        public String getAuthor() {
            return "BG-Software";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }

        @Override
        public String onPlaceholderRequest(Player p, String params) {
            OfflinePlayer offlinePlayer = p != null ? Bukkit.getOfflinePlayer(p.getUniqueId()) : null;
            return onRequest(offlinePlayer, params);
        }

        @Override
        public String onRequest(OfflinePlayer p, String params) {
            String[] split = params.split("_");
            ObjectCache cache = new ObjectCache();
            if (p != null)
                SuperiorPrisonPlugin.getInstance().getPrisonerController().getPrisoner(p.getUniqueId()).ifPresent(cache::add);
            return parser.parse(split, cache);
        }
    }
}
