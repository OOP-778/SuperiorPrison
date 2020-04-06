package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.data.mine.flags.Flag;
import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.booster.MoneyBooster;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.api.requirement.RequirementException;
import com.bgsoftware.superiorprison.plugin.menu.access.AccessObject;
import com.bgsoftware.superiorprison.plugin.menu.access.SortMethod;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.mine.shop.SShopItem;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.booster.SBooster;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SRank;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bgsoftware.superiorprison.plugin.util.TextUtil.beautifyDouble;

public class PlaceholderController {

    private Map<Class<?>, Set<OPair<String, Function<?, String>>>> placeholders = Maps.newHashMap();
    public PlaceholderController() {
        add(SNormalMine.class, "{mine_name}", SNormalMine::getName);
        add(SNormalMine.class, "{mine_prisoners_count}", mine -> mine.getPrisoners().size());

        add(SArea.class, "{area_name}", area -> Helper.beautify(area.getType().name()));

        add(Rank.class, "{rank_prefix}", Rank::getPrefix);
        add(Rank.class, "{rank_name}", Rank::getName);
        add(LadderRank.class, "{rank_order}", LadderRank::getOrder);

        add(Prestige.class, "{prestige_prefix}", Prestige::getPrefix);
        add(Prestige.class, "{prestige_name}", Prestige::getName);
        add(Prestige.class, "{prestige_order}", Prestige::getOrder);

        add(SPrisoner.class, "{prisoner_name}", prisoner -> prisoner.getOfflinePlayer().getName());
        add(SPrisoner.class, "{prisoner_ladder_ranks}", prisoner -> listToString(prisoner.getLadderRanks().stream().map(Rank::getName).collect(Collectors.toList())));
        add(SPrisoner.class, "{prisoner_special_ranks}", prisoner -> listToString(prisoner.getSpecialRanks().stream().map(Rank::getName).collect(Collectors.toList())));
        add(SPrisoner.class, "{prisoner_prestiges}", prisoner -> listToString(prisoner.getPrestiges().stream().map(Prestige::getName).collect(Collectors.toList())));


        add(SNormalMine.class, "{mine_spawnpoint_x}", mine -> mine.getSpawnPoint().isPresent() ? mine.getSpawnPoint().get().x() + "" : "None");
        add(SNormalMine.class, "{mine_spawnpoint_y}", mine -> mine.getSpawnPoint().isPresent() ? mine.getSpawnPoint().get().y() + "" : "None");
        add(SNormalMine.class, "{mine_spawnpoint_z}", mine -> mine.getSpawnPoint().isPresent() ? mine.getSpawnPoint().get().z() + "" : "None");

        // Placeholders for shop
        add(SShopItem.class, "{item_price}", item -> beautifyDouble(item.getPrice()));
        add(SShopItem.class, "{item_name}", item -> TextUtil.beautifyName(item.getItem()));
        add(SortMethod.class, "{sort_method}", method -> TextUtil.beautify(method.name()));

        // Placeholders for boosters
        add(SBooster.class, "{booster_id}", SBooster::getId);
        add(SBooster.class, "{booster_type}", booster -> booster instanceof MoneyBooster ? "money" : "drops");
        add(SBooster.class, "{booster_rate}", SBooster::getRate);
        add(SBooster.class, "{booster_time}", booster -> TimeUtil.leftToString(TimeUtil.getDate(booster.getValidTill())));

        add(Flag.class, "{flag_name}", flag -> Helper.beautify(flag.name()));
        add(Flag.class, "{flag_description}", Flag::getDescription);

        add(RequirementException.class, "{requirement_type}", ex -> TextUtil.beautify(ex.getData().getType()));
        add(RequirementException.class, "{requirement_current}", ex -> TextUtil.beautify(ex.getCurrentValue()));
        add(RequirementException.class, "{requirement_expected}", ex -> TextUtil.beautify(ex.getRequired()));
    }

    private <T> void add(Class<T> type, String placeholder, Function<T, Object> handler) {
        Set<OPair<String, Function<?, String>>> oPairs = placeholders.computeIfAbsent(type, (clazz) -> Sets.newHashSet());
        oPairs.add(new OPair(placeholder, handler));
    }

    public String parse(String text, Object object) {
        return TextUtil.replaceText(object, text, findPlaceholdersFor(object));
    }

    public List<String> parse(List<String> multipleText, Object object) {
        return TextUtil.replaceList(object, multipleText, findPlaceholdersFor(object));
    }

    public Set<OPair<String, Function<Object, String>>> findPlaceholdersFor(Object object) {
        return findPlaceholdersFor(object.getClass());
    }

    public Set<OPair<String, Function<Object, String>>> findPlaceholdersFor(Class clazz) {
        Set<OPair<String, Function<Object, String>>> found = Sets.newHashSet();
        placeholders.forEach((k, v) -> {
            if (k.isAssignableFrom(clazz))
                found.addAll(v.stream().map(pair -> new OPair<>(pair.getFirst(), (Function<Object, String>) pair.getSecond())).collect(Collectors.toList()));

        });
        return found;
    }

    public Map<Class, Set<OPair<String, Function<Object, String>>>> findPlaceholdersFor(Object... objects) {
        Map<Class, Set<OPair<String, Function<Object, String>>>> found = Maps.newHashMap();
        for (Object object : objects)
            found.computeIfAbsent(object.getClass(), object2 -> findPlaceholdersFor(object));

        return found;
    }

    private String listToString(List<String> list) {
        if (list.isEmpty()) return "None";
        return String.join(", ", list);
    }
}
