package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
import com.bgsoftware.superiorprison.plugin.menu.ranks.SortMethod;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.mine.shop.SShopItem;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.bgsoftware.superiorprison.plugin.util.TextUtil.beautifyDouble;

public class PlaceholderController {

    private Map<Class<?>, Set<OPair<String, BiFunction<String, ?, String>>>> placeholders = Maps.newHashMap();

    public PlaceholderController() {
        add(SNormalMine.class, "{mine_name}", (text, mine) -> text.replace("{mine_name}", mine.getName()));
        add(SNormalMine.class, "{mine_prisoners_count}", (text, mine) -> text.replace("{mine_prisoners_count}", mine.getPrisoners().size() + ""));

        add(SArea.class, "{area_name}", (text, area) -> text.replace("{area_name}", Helper.beautify(area.getType().name())));
        add(Rank.class, "{rank_prefix}", (text, rank) -> text.replace("{rank_prefix}", rank.getPrefix()));
        add(Rank.class, "{rank_name}", (text, rank) -> text.replace("{rank_name}", rank.getName()));

        add(SNormalMine.class, "{mine_spawnpoint_x}", (text, mine) -> text.replace("{mine_spawnpoint_x}", mine.getSpawnPoint().isPresent() ? mine.getSpawnPoint().get().x() + "" : "None"));
        add(SNormalMine.class, "{mine_spawnpoint_y}", (text, mine) -> text.replace("{mine_spawnpoint_y}", mine.getSpawnPoint().isPresent() ? mine.getSpawnPoint().get().y() + "" : "None"));
        add(SNormalMine.class, "{mine_spawnpoint_z}", (text, mine) -> text.replace("{mine_spawnpoint_z}", mine.getSpawnPoint().isPresent() ? mine.getSpawnPoint().get().z() + "" : "None"));

        // Placeholders for shop
        add(SNormalMine.class, "{mine_shop_items_count}", (text, mine) -> text.replace("{mine_shop_items_count}", mine.getShop().getItems().size() + ""));
        add(SShopItem.class, "{item_price}", (text, item) -> text.replace("{item_price}", beautifyDouble(item.getPrice())));
        add(SShopItem.class, "{item_name}", (text, item) -> text.replace("{item_name}", TextUtil.beautifyName(item.getItem())));
        add(SortMethod.class, "{sort_method}", (text, method) -> text.replace("{sort_method}", TextUtil.beautify(method.name())));

    }

    private <T> void add(Class<T> type, String placeholder, BiFunction<String, T, String> handler) {
        Set<OPair<String, BiFunction<String, ?, String>>> oPairs = placeholders.computeIfAbsent(type, (clazz) -> Sets.newHashSet());
        oPairs.add(new OPair<>(placeholder, handler));
    }

    public String parse(String text, Object object) {
        return TextUtil.replaceText(object, text, findPlaceholdersFor(object));
    }

    public List<String> parse(List<String> multipleText, Object object) {
        return TextUtil.replaceList(object, multipleText, findPlaceholdersFor(object));
    }

    public <T extends Object> Set<BiFunction<String, T, String>> findPlaceholdersFor(T object) {
        Set<BiFunction<String, T, String>> found = Sets.newHashSet();
        placeholders.forEach((k, v) -> {
            if (k.isAssignableFrom(object.getClass()))
                found.addAll(v.stream().map(pair -> (BiFunction<String, T, String>) pair.getSecond()).collect(Collectors.toSet()));

        });
        return found;
    }

    public Set<BiFunction<String, Object, String>> findPlaceholdersFor(Object... objects) {
        Set<BiFunction<String, Object, String>> found = Sets.newHashSet();
        for (Object object : objects)
            found.addAll(findPlaceholdersFor(object));

        return found;
    }


}
