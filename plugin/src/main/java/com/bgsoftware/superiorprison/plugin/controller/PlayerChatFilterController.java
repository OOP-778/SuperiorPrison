package com.bgsoftware.superiorprison.plugin.controller;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.Function;

public class PlayerChatFilterController {
    private final Set<UUID> filteredPlayers = new HashSet<>();
    private final Set<Function<String, Boolean>> filters = new HashSet<>();

    public PlayerChatFilterController(List<String> rawFilters) {
        for (String rawFilter : rawFilters) {
            String[] split = rawFilter.split(":");
            String filterType = split[0];
            String filterInput = split[1];

            addFilter(filterType, filterInput);
        }
    }

    public boolean validate(String content) {
        for (Function<String, Boolean> filter : filters) {
            if (filter.apply(content))
                return false;
        }

        return true;
    }

    public boolean isFiltered(UUID uuid) {
        return filteredPlayers.contains(uuid);
    }

    public void filter(UUID uuid) {
        filteredPlayers.add(uuid);
    }

    public void unfilter(UUID uuid) {
        filteredPlayers.remove(uuid);
    }

    private void addFilter(String type, String input) {
        type = type.toLowerCase(Locale.ROOT);
        switch (type) {
            case "contains":
                filters.add(in -> in.toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT)));
                break;

            case "starts":
                filters.add(in -> in.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT)));
                break;
        }
    }
}
