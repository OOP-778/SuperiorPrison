package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.type.NormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class PlaceholderController {

    private Set<BiFunction<SNormalMine, String, String>> placeholders = Sets.newHashSet();

    public PlaceholderController() {
        placeholders.add((mine, currentText) -> currentText.replace("%mine_name%", mine.getName()));
        placeholders.add((mine, currentText) -> currentText.replace("%mine_permission%", mine.getPermission().orElse("Not set")));
        placeholders.add((mine, currentText) -> currentText.replace("%mine_icon_displayname%", mine.getIcon().getItemMeta().getDisplayName()));

        // Placeholders for min point
        placeholders.add((mine, currentText) -> currentText.replace("%mine_minpoint_x%", mine.getMinPoint().x() + ""));
        placeholders.add((mine, currentText) -> currentText.replace("%mine_minpoint_y%", mine.getMinPoint().y() + ""));
        placeholders.add((mine, currentText) -> currentText.replace("%mine_minpoint_z%", mine.getMinPoint().z() + ""));

        // Placeholders for highpoint
        placeholders.add((mine, currentText) -> currentText.replace("%mine_highpoint_x%", mine.getHighPoint().x() + ""));
        placeholders.add((mine, currentText) -> currentText.replace("%mine_highpoint_y%", mine.getHighPoint().y() + ""));
        placeholders.add((mine, currentText) -> currentText.replace("%mine_highpoint_z%", mine.getHighPoint().z() + ""));

        // Placeholders for spawn
        placeholders.add((mine, currentText) -> currentText.replace("%mine_spawnpoint_x%", mine.getSpawnPoint().isPresent() ? mine.getSpawnPoint().get().x() + "" : "Not set"));
        placeholders.add((mine, currentText) -> currentText.replace("%mine_spawnpoint_y%", mine.getSpawnPoint().isPresent() ? mine.getSpawnPoint().get().y() + "" : "Not set"));
        placeholders.add((mine, currentText) -> currentText.replace("%mine_spawnpoint_z%", mine.getSpawnPoint().isPresent() ? mine.getSpawnPoint().get().y() + "" : "Not set"));

        // Placeholders for shop
        placeholders.add((mine, currentText) -> currentText.replace("%mine_shop_title%", mine.getShop().getTitle()));
    }

    public String parse(String text, SNormalMine mine) {
        System.out.println("Trying to parse text: " + text);
        for (BiFunction<SNormalMine, String, String> function : placeholders)
            text = function.apply(mine, text);

        return text;
    }

    public List<String> parse(List<String> multipleText, SNormalMine mine) {
        System.out.println("Trying to parse multiple text: " + multipleText);
        List<String> parsed = new ArrayList<>();
        for (String text : multipleText) {
            for (BiFunction<SNormalMine, String, String> function : placeholders)
                text = function.apply(mine, text);

            parsed.add(text);
        }
        return parsed;
    }

}
