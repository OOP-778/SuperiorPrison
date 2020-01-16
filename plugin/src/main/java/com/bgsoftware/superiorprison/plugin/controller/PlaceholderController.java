package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.ReplacerUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class PlaceholderController {

    private Map<Class<?>, Set<OPair<String, BiFunction<String, ?, String>>>> placeholders = Maps.newHashMap();

    public PlaceholderController() {
        add(SNormalMine.class, "{mine_name}", (text, mine) -> text.replace("{mine_name}", mine.getName()));
        add(SNormalMine.class, "{mine_permission}", (text, mine) -> text.replace("{mine_permission}", mine.getPermission().orElse("None")));

        add(SNormalMine.class, "{mine_minpoint_x}", (text, mine) -> text.replace("{mine_minpoint_x}", mine.getMinPoint().x() + ""));
        add(SNormalMine.class, "{mine_minpoint_y}", (text, mine) -> text.replace("{mine_minpoint_y}", mine.getMinPoint().y() + ""));
        add(SNormalMine.class, "{mine_minpoint_z}", (text, mine) -> text.replace("{mine_minpoint_z}", mine.getMinPoint().z() + ""));

        add(SNormalMine.class, "{mine_highpoint_x}", (text, mine) -> text.replace("{mine_highpoint_x}", mine.getHighPoint().x() + ""));
        add(SNormalMine.class, "{mine_highpoint_y}", (text, mine) -> text.replace("{mine_highpoint_y}", mine.getHighPoint().y() + ""));
        add(SNormalMine.class, "{mine_highpoint_z}", (text, mine) -> text.replace("{mine_highpoint_z}", mine.getHighPoint().z() + ""));

        add(SNormalMine.class, "{mine_spawnpoint_x}", (text, mine) -> text.replace("{mine_spawnpoint_x}", mine.getSpawnPoint().isPresent() ? mine.getSpawnPoint().get().x() + "" : "None"));
        add(SNormalMine.class, "{mine_spawnpoint_y}", (text, mine) -> text.replace("{mine_spawnpoint_y}", mine.getSpawnPoint().isPresent() ? mine.getSpawnPoint().get().y() + "" : "None"));
        add(SNormalMine.class, "{mine_spawnpoint_z}", (text, mine) -> text.replace("{mine_spawnpoint_z}", mine.getSpawnPoint().isPresent() ? mine.getSpawnPoint().get().z() + "" : "None"));

        // Placeholders for shop
        add(SNormalMine.class, "{mine_shop_title}", (text, mine) -> text.replace("{mine_shop_title}", mine.getShop().getTitle()));
    }

    private <T> void add(Class<T> type, String placeholder, BiFunction<String, T, String> handler) {
        Set<OPair<String, BiFunction<String, ?, String>>> oPairs = placeholders.computeIfAbsent(type, (clazz) -> Sets.newHashSet());
        oPairs.add(new OPair<>(placeholder, handler));
    }

    public String parse(String text, Object object) {
        return ReplacerUtils.replaceText(object, text, findPlaceholdersFor(object), Optional.empty());
    }

    public List<String> parse(List<String> multipleText, Object object) {
        return ReplacerUtils.replaceList(object, multipleText, findPlaceholdersFor(object), Optional.empty());
    }

    public <T extends Object> Set<BiFunction<String, T, String>> findPlaceholdersFor(T object) {
        Set<BiFunction<String, T, String>> found = Sets.newHashSet();
        placeholders.forEach((k, v) -> {
            if (k.isAssignableFrom(object.getClass()))
                found.addAll(v.stream().map(pair -> (BiFunction<String, T, String>) pair.getSecond()).collect(Collectors.toSet()));

        });
        return found;
    }

    public Set<BiFunction<String, Object, String>> findPlaceholdersFor(Object ...objects) {
        Set<BiFunction<String, Object, String>> found = Sets.newHashSet();
        for (Object object : objects)
            found.addAll(findPlaceholdersFor(object));

        return found;
    }


}
