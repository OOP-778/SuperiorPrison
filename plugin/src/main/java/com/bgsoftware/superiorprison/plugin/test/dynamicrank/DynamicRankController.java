package com.bgsoftware.superiorprison.plugin.test.dynamicrank;

import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class DynamicRankController {
    private final Map<UUID, Map<String, DynamicRankTemplate>> cachedRanks = new HashMap<>();
    private final Map<Character, DynamicRankTemplate> loadedRanks = new HashMap<>();

    private final List<Character> availableRanks = new ArrayList<>();
    private final DynamicRankTemplate template;

    public DynamicRankController(Config config) {
        Optional<ConfigSection> mode = config.getSection("mode");
        ConfigSection configSection = mode.get();

        String start = configSection.getAs("start");
        String end = configSection.getAs("end");

        template = new DynamicRankTemplate(configSection);

        AtomicInteger orderCounter = new AtomicInteger();
        IntStream
                .rangeClosed(start.toCharArray()[0], end.toCharArray()[0])
                .mapToObj(i -> (char) i)
                .forEach(c -> {
                    DynamicRankTemplate clone = template.clone();
                    clone.setOrder(orderCounter.incrementAndGet());
                    clone.replace("%rank_name%", c);
                    clone.replace("%rank_order%", clone.getOrder() + "");
                    loadedRanks.put(c, clone);
                });
    }
}
